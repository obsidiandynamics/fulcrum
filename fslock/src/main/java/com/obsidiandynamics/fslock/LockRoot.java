package com.obsidiandynamics.fslock;

import static com.obsidiandynamics.func.Functions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import com.obsidiandynamics.func.*;

/**
 *  Embodiment of the <b>FS.lock</b> reentrant, interprocess exclusive locking protocol. <p>
 *  
 *  FS.lock builds on the {@link java.nio.channels.FileLock} class, as the underlying cross-platform locking primitive.
 *  Locking is performed across two tiers: <b>root</b> and <b>node</b>. The root lock resides in a user-chosen
 *  directory (completely arbitrary, subject to file permissions) under the filename {@code .lock}.
 *  A node occupies a subdirectory immediately under the root directory; the node lock filename takes
 *  the form {@code node/.lock}, where {@code node} is the directory name. The node directory may also
 *  contain arbitrary user data; in which case the node lock can be perceived as an exclusive mutex over
 *  the persisted data. <p>
 *  
 *  When acquiring a node lock, the root lock is temporary acquired first. If the root lock is held
 *  by another JVM process (or another thread in the current process), acquisition aborts and may
 *  be re-attempted immediately thereafter (but typically after a backoff interval). If the root lock is
 *  acquired, the node lock acquisition is subsequently attempted. If successful, the {@link DirectoryLock} 
 *  is wrapped in a {@link ReentrantDirectoryLock} and returned to the caller. Whether or not the 
 *  node lock acquisition is successful, the root lock is subsequently released. <p>
 *  
 *  A node directory may be 'vacuumed'. This is essentially a mechanism for deleting an entire node
 *  directory (recursively) if no other process holds the node lock; used to purge user data that would
 *  otherwise be protected by a node lock. Vacuuming involves holding the root lock while recursively
 *  deleting the node directory. The root lock acts as barrier, preventing concurrent lock acquisition.
 *  (So that a node lock doesn't get re-acquired while the node directory is in the midst of being
 *  wiped.) The two-tier system is required to guarantee safe vacuuming. (Without vacuuming, a single-tier
 *  node lock would have sufficed.) Locks are acquired in a strictly top-down fashion — the root lock is
 *  always acquired first, followed by the node lock. A process never acquires a node lock without having
 *  secured the root lock first. <p>
 *  
 *  The process <em>must</em> ensure that a root lock is effectively singleton. Two root locks at the
 *  same location cannot coexist in the same process, as the {@link LockRoot} contains the definitive
 *  ledger of all node locks held under its province, the owner threads, entry counts and so forth. <p>
 *  
 *  This class is thread-safe.
 */
public final class LockRoot {
  private static final int DEF_ACQUIRE_BACKOFF_MILLIS = 10;

  private final File rootDir;
  
  private final Map<String, DirectoryLock> locks = new HashMap<>();
  
  private final Object rootMutex = new Object();
  
  private final LockProvider lockProvider;
  
  /**
   *  Creates a new lock root housed in the given directory. The directory will be created if necessary.
   *  
   *  @param rootDir The root directory.
   */
  public LockRoot(File rootDir) {
    this(rootDir, RandomAccessFileLockProvider.getInstance());
  }
  
  LockRoot(File rootDir, LockProvider lockProvider) {
    mustExist(rootDir, illegalArgument("Root directory cannot be null"));
    mustExist(lockProvider, illegalArgument("Lock provider cannot be null"));
    this.rootDir = rootDir;
    this.lockProvider = lockProvider;
    
    if (! rootDir.exists()) {
      //noinspection ResultOfMethodCallIgnored
      rootDir.mkdirs();
    } else {
      DirectoryLock.ensureIsDirectory(rootDir);
    }
  }
  
  LockProvider getLockProvider() {
    return lockProvider;
  }
  
  <R, X extends Throwable> R runInMutex(CheckedFunction<Map<String, DirectoryLock>, R, X> exclusiveOperator) throws X {
    synchronized (rootMutex) {
      return exclusiveOperator.apply(locks);
    }
  }
  
  /**
   *  Obtains an unmodifiable copy of the underlying locks map.
   *  
   *  @return A snapshot mapping of absolute node paths to {@link DirectoryLock} objects.
   */
  public Map<String, DirectoryLock> getLocks() {
    final Map<String, DirectoryLock> locksCopy;
    synchronized (rootMutex) {
      locksCopy = new HashMap<>(locks);
    }
    return Collections.unmodifiableMap(locksCopy);
  }
  
  File dirForNode(String nodeName) {
    return new File(rootDir.getPath() + File.separator + nodeName);
  }
  
  /**
   *  Attempts to acquire a node lock.
   *  
   *  @param nodeName The name of the node.
   *  @return The {@link ReentrantDirectoryLock} instance if acquired, or {@code null} if acquisition fails.
   *  @throws IOException If an I/O error occurs.
   */
  public ReentrantDirectoryLock tryAcquire(String nodeName) throws IOException {
    mustExist(nodeName, illegalArgument("Node name cannot be null"));
    final ReentrantDirectoryLock lock;
    try (ReentrantDirectoryLock rootLock = DirectoryLock.tryAcquire(this, rootDir)) {
      if (rootLock != null) {
        final File lockDir = dirForNode(nodeName);
        lock = DirectoryLock.tryAcquire(this, lockDir);
      } else {
        lock = null;
      }
    }
    return lock;
  }
  
  /**
   *  Acquires a node lock, blocking indefinitely until the lock is acquired or the calling thread is
   *  interrupted.
   *  
   *  @param nodeName The name of the node.
   *  @return The {@link ReentrantDirectoryLock} instance.
   *  @throws IOException If an I/O error occurs.
   *  @throws InterruptedException If the thread is interrupted.
   */
  public ReentrantDirectoryLock acquire(String nodeName) throws IOException, InterruptedException {
    return mustExist(acquire(nodeName, DEF_ACQUIRE_BACKOFF_MILLIS, Integer.MAX_VALUE));
  }

  /**
   *  Awaits a bounded period of time for the acquisition of a node lock, returning when the lock is
   *  acquired, the thread is interrupted or if the operation times out.
   *  
   *  @param nodeName The name of the node.
   *  @param backoffMillis The backoff interval (in milliseconds).
   *  @param timeoutMillis The upper bound on the wait time (in milliseconds).
   *  @return The {@link ReentrantDirectoryLock} instance if acquired, or {@code null} if the operation times out.
   *  @throws IOException If an I/O error occurs.
   *  @throws InterruptedException If the thread is interrupted.
   */
  public ReentrantDirectoryLock acquire(String nodeName, int backoffMillis, int timeoutMillis) throws IOException, InterruptedException {
    mustExist(nodeName, illegalArgument("Node name cannot be null"));
    mustBeGreaterOrEqual(backoffMillis, 0, illegalArgument("Backoff time must be greater or equal to 0"));
    mustBeGreaterOrEqual(timeoutMillis, 0, illegalArgument("Timeout must be greater or equal to 0"));
    
    final long maxWait = System.currentTimeMillis() + timeoutMillis;
    final int waitTime = Math.min(backoffMillis, timeoutMillis);
    for (;;) {
      final ReentrantDirectoryLock lock = tryAcquire(nodeName);
      if (lock != null) {
        return lock;
      } else if (System.currentTimeMillis() < maxWait) {
        //noinspection BusyWait
        Thread.sleep(waitTime);
      } else {
        return null;
      }
    }
  }
  
  /**
   *  Attempts to vacuum of all node directories under this root, returning the actual count of the successfully
   *  vacuumed nodes.
   *  
   *  @return The number of nodes vacuumed.
   *  @throws IOException If an I/O error occurs.
   */
  public int vacuumAll() throws IOException {
    int vacuumed = 0;
    for (File fileWithinRoot : Functions.mustExist(rootDir.listFiles(), IOException::new)) {
      if (fileWithinRoot.isDirectory()) {
        if (vacuum(fileWithinRoot)) {
          vacuumed++;
        }
      }
    }
    return vacuumed;
  }
  
  /**
   *  Attempts to vacuum a node directory, provided that no other threads or processes presently holds the node lock.
   *  
   *  @param nodeName The node name.
   *  @return True if the node was vacuumed, or false if the node lock was not vacant.
   *  @throws IOException If an I/O error occurs.
   */
  public boolean vacuum(String nodeName) throws IOException {
    mustExist(nodeName, illegalArgument("Node name cannot be null"));
    return vacuum(dirForNode(nodeName));
  }
  
  private boolean vacuum(File nodeDir) throws IOException {
    DirectoryLock.ensureIsDirectory(nodeDir);
    
    final boolean vacuumed;
    try (ReentrantDirectoryLock rootLock = DirectoryLock.tryAcquire(this, rootDir)) {
      if (rootLock != null) {
        final boolean acquiredNodeLock;
        try (ReentrantDirectoryLock lock = DirectoryLock.tryAcquire(this, nodeDir)) {
          // only treat a lock as acquired if we're the first owner (this prevents vacuuming by a 
          // reentrant caller)
          acquiredNodeLock = lock != null && lock.getLock().getCount() == 1;
        }
        
        if (acquiredNodeLock) {
          try (Stream<Path> streamPath = Files.walk(nodeDir.toPath()).sorted(Comparator.reverseOrder())) {
            try (Stream<File> stream = streamPath.map(Path::toFile)) {
              //noinspection ResultOfMethodCallIgnored
              stream.forEach(File::delete);
            }
          }
          vacuumed = true;
        } else {
          vacuumed = false;
        }
      } else {
        vacuumed = false;
      }
    }
    return vacuumed;
  }

  @Override
  public String toString() {
    return LockRoot.class.getSimpleName() + " [rootDir=" + rootDir + ", locks=" + locks + "]";
  }
}
