package com.obsidiandynamics.fslock;

import static com.obsidiandynamics.func.Functions.*;

import java.io.*;

import com.obsidiandynamics.format.*;

/**
 *  The manifestation of a raw directory lock, which can be conveniently used for locking either
 *  the root or the node directory.
 */
public final class DirectoryLock {
  private final LockRoot root;

  private final Closeable lock;

  private final String absolutePath;
  
  private final Thread owner;
  
  private int count;

  private DirectoryLock(LockRoot root, Closeable lock, String absolutePath, Thread owner) {
    this.root = root;
    this.lock = lock;
    this.absolutePath = absolutePath;
    this.owner = owner;
  }
  
  public int getCount() {
    return count;
  }
  
  public Thread getOwner() {
    return owner;
  }
  
  public String getAbsolutePath() {
    return absolutePath;
  }
  
  void enter() {
    count++;
  }
  
  boolean isCurrentThreadOwner() {
    return Thread.currentThread() == owner;
  }

  void leave(boolean threadIsOwner) throws IOException {
    mustBeTrue(! threadIsOwner || isCurrentThreadOwner(), illegalState("Current thread not owner"));
    mustBeGreater(count, 0, illegalState("Count must be greater than 0"));
    count--;
    if (count == 0) {
      lock.close();
      root.runInMutex(acquiredPaths -> {
        acquiredPaths.remove(absolutePath);
        return null;
      });
    }
  }
  
  String baseToString() {
    return "absolutePath=" + absolutePath + ", owner=" + owner + ", count=" + count;
  }
  
  @Override
  public String toString() {
    return DirectoryLock.class.getSimpleName() + " [" + baseToString() + "]";
  }

  static void ensureIsFile(File assumedFile) {
    mustBeTrue(assumedFile.isFile(), withMessage(() -> assumedFile + " is not a file", IllegalArgumentException::new));
  }

  static void ensureIsDirectory(File assumedDir) {
    mustBeTrue(assumedDir.isDirectory(), withMessage(() -> assumedDir + " is not a directory", IllegalArgumentException::new));
  }

  static File lockFileForDir(File dir) {
    return new File(dir.getPath() + File.separator + ".lock");
  }

  private static Closeable tryLock(LockRoot root, File dir) throws IOException {
    if (! dir.exists()) {
      mustBeTrue(dir.mkdirs(), withMessage(SafeFormat.supply("Could not create directory %s", dir), IOException::new));
    } else {
      ensureIsDirectory(dir);
    }

    final File lockFile = lockFileForDir(dir);
    if (lockFile.exists()) {
      ensureIsFile(lockFile);
    }

    return root.getLockProvider().tryLock(lockFile);
  }

  static ReentrantDirectoryLock tryAcquire(LockRoot root, File dir) throws IOException {
    final String lockAbsolutePath = dir.getAbsolutePath();
    final DirectoryLock acquiredLock = root.runInMutex(locks -> {
      final DirectoryLock existingLock = locks.get(lockAbsolutePath);
      if (existingLock != null && existingLock.isCurrentThreadOwner()) {
        existingLock.enter();
        return existingLock;
      } else if (existingLock == null) {
        Closeable lock = null;
        try {
          lock = tryLock(root, dir);
          if (lock != null) {
            final DirectoryLock nodeLock = new DirectoryLock(root, lock, lockAbsolutePath, Thread.currentThread());
            nodeLock.enter();
            locks.put(lockAbsolutePath, nodeLock);
            return nodeLock;
          } else {
            return null;
          }
        } finally {
          if (lock == null) {
            locks.remove(lockAbsolutePath);
          }
        }
      } else {
        return null;
      }
    });
    
    return ifPresent(acquiredLock, ReentrantDirectoryLock::new);
  }
}
