package com.obsidiandynamics.fslock;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.assertj.core.api.*;
import org.junit.*;
import org.junit.runners.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class LockRootTest {
  private static final String SANDBOX_DIR = System.getProperty("java.io.tmpdir") + File.separator + "sandbox";
  
  @Test
  public void testInit_createDirectory() {
    final File rootDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "LockRootTest-createDirectory");
    if (rootDir.exists()) {
      rootDir.delete();
    }
    new LockRoot(rootDir);
    assertTrue(rootDir.exists());
    rootDir.delete();
  }
  
  @Test
  public void testTryAcquireClose_oneThread_successWithRepeatedAcquisition() throws IOException {
    final LockRoot lockRoot = new LockRoot(new File(SANDBOX_DIR));
    final String nodeName = UUID.randomUUID().toString();
    final File lockFile = DirectoryLock.lockFileForDir(lockRoot.dirForNode(nodeName));
    try (ReentrantDirectoryLock lock = lockRoot.tryAcquire(nodeName)) {
      assertNotNull(lock);
      assertTrue(lockFile.isFile());
      final String expectedAbsolutePath = lockRoot.dirForNode(nodeName).getAbsolutePath();
      assertEquals(expectedAbsolutePath, lock.getLock().getAbsolutePath());
    }
    
    try (ReentrantDirectoryLock lock = lockRoot.tryAcquire(nodeName)) {
      assertNotNull(lock);
    }
    assertTrue(lockFile.isFile());
  }
  
  @Test
  public void testTryAcquireClose_oneThread_failOnRoot() throws IOException {
    final File rootLockFile = new File(SANDBOX_DIR + File.separator + ".lock");
    final LockProvider lockProvider = file -> {
      if (file.getAbsolutePath().equals(rootLockFile.getAbsolutePath())) {
        return null;
      } else {
        return RandomAccessFileLockProvider.getInstance().tryLock(file);
      }
    };
    final LockRoot lockRoot = new LockRoot(new File(SANDBOX_DIR), lockProvider);
    final String nodeName = UUID.randomUUID().toString();
    final File nodeLockFile = DirectoryLock.lockFileForDir(lockRoot.dirForNode(nodeName));
    try (ReentrantDirectoryLock lock = lockRoot.tryAcquire(nodeName)) {
      assertNull(lock);
      assertFalse(nodeLockFile.exists());
    }
  }
  
  @Test
  public void testTryAcquireClose_oneThread_failOnNode() throws IOException {
    final String nodeName = UUID.randomUUID().toString();
    final File nodeLockFile = DirectoryLock.lockFileForDir(new File(SANDBOX_DIR + File.separator + nodeName));
    final LockProvider lockProvider = file -> {
      if (file.getAbsolutePath().equals(nodeLockFile.getAbsolutePath())) {
        return null;
      } else {
        return RandomAccessFileLockProvider.getInstance().tryLock(file);
      }
    };
    final LockRoot lockRoot = new LockRoot(new File(SANDBOX_DIR), lockProvider);
    try (ReentrantDirectoryLock lock = lockRoot.tryAcquire(nodeName)) {
      assertNull(lock);
      assertFalse(nodeLockFile.exists());
    }
  }
  
  @Test
  public void testAcquire_successWithMultipleRelease() throws IOException, InterruptedException {
    final LockRoot lockRoot = new LockRoot(new File(SANDBOX_DIR));
    final String nodeName = UUID.randomUUID().toString();
    try (ReentrantDirectoryLock lock = lockRoot.acquire(nodeName)) {
      assertNotNull(lock);
      assertEquals(1, lock.getLock().getCount());
      
      // premature release
      lock.release();
      assertEquals(0, lock.getLock().getCount());
      
      // subsequent release should have no further effect
      lock.release();
      assertEquals(0, lock.getLock().getCount());
    }
  }
  
  @Test
  public void testTryAcquire_reentrant() throws IOException, InterruptedException {
    final LockRoot lockRoot = new LockRoot(new File(SANDBOX_DIR));
    final String nodeName = UUID.randomUUID().toString();
    try (ReentrantDirectoryLock lock = lockRoot.tryAcquire(nodeName)) {
      assertEquals(1, lock.getLock().getCount());
      try (ReentrantDirectoryLock lock2 = lockRoot.tryAcquire(nodeName)) {
        assertNotSame(lock, lock2);
        assertSame(lock.getLock(), lock2.getLock());
        assertEquals(2, lock.getLock().getCount());
      }
      assertEquals(1, lock.getLock().getCount());
    }
  }
  
  @Test
  public void testTryAcquire_leaveInWrongThread_ownerCheckEnabled() throws IOException, InterruptedException {
    final LockRoot lockRoot = new LockRoot(new File(SANDBOX_DIR));
    final String nodeName = UUID.randomUUID().toString();
    try (ReentrantDirectoryLock lock = lockRoot.tryAcquire(nodeName)) {
      assertEquals(1, lock.getLock().getCount());
      final AtomicReference<Throwable> expectedExceptionRef = new AtomicReference<>();
      final Thread thread = new Thread(() -> {
        try {
          lock.release();
        } catch (Throwable e) {
          expectedExceptionRef.set(e);
        }
      });
      thread.start();
      thread.join();
      
      Assertions.assertThat(expectedExceptionRef.get())
      .isInstanceOf(IllegalStateException.class).hasMessage("Current thread not owner");

      assertEquals(1, lock.getLock().getCount());
    }
  }
  
  @Test
  public void testTryAcquire_leaveInWrongThread_ownerCheckSuppressed() throws IOException, InterruptedException {
    final LockRoot lockRoot = new LockRoot(new File(SANDBOX_DIR));
    final String nodeName = UUID.randomUUID().toString();
    try (ReentrantDirectoryLock lock = lockRoot.tryAcquire(nodeName)) {
      assertEquals(1, lock.getLock().getCount());
      final AtomicReference<Throwable> errorRef = new AtomicReference<>();
      final Thread thread = new Thread(() -> {
        try {
          lock.release(false);
        } catch (Throwable e) {
          errorRef.set(e);
        }
      });
      thread.start();
      thread.join();
      
      assertNull(errorRef.get());
      assertEquals(0, lock.getLock().getCount());
    }
  }
  
  @Test
  public void testAcquire_timeout() throws IOException, InterruptedException {
    final LockRoot lockRoot = new LockRoot(new File(SANDBOX_DIR));
    final String nodeName = UUID.randomUUID().toString();
    try (ReentrantDirectoryLock lock = lockRoot.tryAcquire(nodeName)) {
      final AtomicReference<Throwable> errorRef = new AtomicReference<>();
      final Thread thread = new Thread(() -> {
        try {
          assertNull(lockRoot.acquire(nodeName, 0, 50));
        } catch (Throwable e) {
          errorRef.set(e);
          e.printStackTrace();
        }
      });
      thread.start();
      thread.join();
      assertNull(errorRef.get());
    }
  }
  
  @Test
  public void testTryAcquireClose_twoThreads() throws IOException, InterruptedException {
    final LockRoot lockRoot = new LockRoot(new File(SANDBOX_DIR));
    final String nodeName = UUID.randomUUID().toString();
    try (ReentrantDirectoryLock lock = lockRoot.tryAcquire(nodeName)) {
      assertNotNull(lock);
      
      final AtomicReference<Throwable> errorRef = new AtomicReference<>();
      final Thread thread = new Thread(() -> {
        try (ReentrantDirectoryLock lock2 = lockRoot.tryAcquire(nodeName)) {
          assertNull(lock2);
        } catch (Throwable e) {
          errorRef.set(e);
          e.printStackTrace();
        }
      });
      thread.start();
      thread.join();
      
      assertNull(errorRef.get());
    }
  }
  
  @Test
  public void testGetLocks() throws IOException, InterruptedException {
    final LockRoot lockRoot = new LockRoot(new File(SANDBOX_DIR));
    final String nodeName = UUID.randomUUID().toString();
    final File nodeDir = lockRoot.dirForNode(nodeName);
    assertEquals(Collections.emptyMap(), lockRoot.getLocks());
    try (ReentrantDirectoryLock lock = lockRoot.acquire(nodeName)) {
      assertNotNull(lock);
      final Map<String, DirectoryLock> locks = lockRoot.getLocks();
      assertEquals(1, locks.size());
      final DirectoryLock nodeLock = locks.get(nodeDir.getAbsolutePath());
      assertNotNull(nodeLock);
      assertEquals(1, nodeLock.getCount());
      assertSame(Thread.currentThread(), nodeLock.getOwner());
      
      Assertions.assertThat(nodeLock.toString())
      .contains("absolutePath=" + nodeDir.getAbsolutePath())
      .contains("owner=" + Thread.currentThread())
      .contains("count=1");
    }
  }
  
  @Test
  public void testToString() throws IOException {
    final LockRoot lockRoot = new LockRoot(new File(SANDBOX_DIR));
    final String nodeName = UUID.randomUUID().toString();
    final File nodeDir = lockRoot.dirForNode(nodeName);
    try (ReentrantDirectoryLock lock = lockRoot.tryAcquire(nodeName)) {
      Assertions.assertThat(lock.toString())
      .contains("absolutePath=" + nodeDir.getAbsolutePath())
      .contains("owner=" + Thread.currentThread())
      .contains("count=1");
    }
    Assertions.assertThat(lockRoot.toString()).contains("rootDir=").contains(new File(SANDBOX_DIR).toString()).contains("locks=");
  }
  
  @Test
  public void testVacuum_successs() throws IOException {
    final LockRoot lockRoot = new LockRoot(new File(SANDBOX_DIR));
    final String nodeName = UUID.randomUUID().toString();
    try (ReentrantDirectoryLock lock = lockRoot.tryAcquire(nodeName)) {
      assertNotNull(lock);
    }
    
    final File lockDir = lockRoot.dirForNode(nodeName);
    assertTrue(lockRoot.vacuum(nodeName));
    assertFalse(lockDir.exists());
  }
  
  @Test
  public void testVacuum_failOnRoot() throws IOException {
    final LockRoot preLockRoot = new LockRoot(new File(SANDBOX_DIR));
    final String nodeName = UUID.randomUUID().toString();
    try (ReentrantDirectoryLock lock = preLockRoot.tryAcquire(nodeName)) {
      assertNotNull(lock);
    }
    
    final File roolLockFile = new File(SANDBOX_DIR + File.separator + ".lock");
    final LockProvider lockProvider = file -> {
      if (file.getAbsolutePath().equals(roolLockFile.getAbsolutePath())) {
        return null;
      } else {
        return RandomAccessFileLockProvider.getInstance().tryLock(file);
      }
    };
    final LockRoot lockRoot = new LockRoot(new File(SANDBOX_DIR), lockProvider);
    final File lockDir = preLockRoot.dirForNode(nodeName);
    assertFalse(lockRoot.vacuum(nodeName));
    assertTrue(lockDir.exists());
  }
  
  @Test
  public void testVacuum_failWithReentrantCaller() throws IOException, InterruptedException {
    final LockRoot lockRoot = new LockRoot(new File(SANDBOX_DIR));
    final String nodeName = UUID.randomUUID().toString();
    try (ReentrantDirectoryLock lock = lockRoot.acquire(nodeName)) {
      assertFalse(lockRoot.vacuum(nodeName));
    }
  }
  
  @Test
  public void testVacuumAll() throws IOException, InterruptedException {
    final LockRoot lockRoot = new LockRoot(new File(SANDBOX_DIR));
    final String releasedNodeName = UUID.randomUUID().toString();
    try (ReentrantDirectoryLock lock = lockRoot.tryAcquire(releasedNodeName)) {
      assertNotNull(lock);
    }

    final String heldNodeName = UUID.randomUUID().toString();
    final File heldNodeDir = lockRoot.dirForNode(heldNodeName);
    try (ReentrantDirectoryLock lock = lockRoot.tryAcquire(heldNodeName)) {
      assertNotNull(lock);
      
      final AtomicReference<Throwable> errorRef = new AtomicReference<>();
      final Thread thread = new Thread(() -> {
        try {
          final File sandboxDir = new File(SANDBOX_DIR);
          final File rootLockFile = new File(SANDBOX_DIR + File.separator + ".lock");
          final File strayFile = new File(SANDBOX_DIR + File.separator + "stray");
          strayFile.createNewFile();
          strayFile.deleteOnExit();
          final File[] subs = sandboxDir.listFiles();
          final int vacuumed = lockRoot.vacuumAll();
          boolean success = false;
          try {
            assertEquals(subs.length - 3, vacuumed);
            success = true;
          } finally {
            if (! success) {
              for (File sub : subs) {
                System.err.println("Sub: " + sub);
              }
              System.err.println("Held: " + heldNodeDir);
              System.err.println("Stray: " + strayFile);
              System.err.println("Remaining: " + Arrays.asList(sandboxDir.listFiles()));
            }
          }
          Assertions.assertThat(sandboxDir.listFiles()).containsExactlyInAnyOrder(rootLockFile, strayFile, heldNodeDir);
        } catch (Throwable e) {
          e.printStackTrace();
          errorRef.set(e);
        }
      });
      thread.start();
      thread.join();
      assertNull(errorRef.get());
    }
  }
}
