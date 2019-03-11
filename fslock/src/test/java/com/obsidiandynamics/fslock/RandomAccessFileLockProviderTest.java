package com.obsidiandynamics.fslock;

import static org.junit.Assert.*;

import java.io.*;
import java.nio.channels.*;

import org.assertj.core.api.*;
import org.junit.*;

public final class RandomAccessFileLockProviderTest {
  @Test
  public void testRandomAccessFileIfLockAcquired() throws FileNotFoundException, IOException {
    final File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "RandomAccessFileLockProviderTest");
    file.deleteOnExit();
    try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
      assertSame(raf, RandomAccessFileLockProvider.randomAccessFileIfLockAcquired(raf, raf.getChannel().tryLock()));
      assertNull(RandomAccessFileLockProvider.randomAccessFileIfLockAcquired(raf, null));
    }
  }
  
  @Test
  public void testCloseRandomAccessFileIfLockNotAcquired() throws FileNotFoundException, IOException {
    final File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "RandomAccessFileLockProviderTest");
    file.deleteOnExit();
    try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
      RandomAccessFileLockProvider.closeRandomAccessFileIfLockNotAcquired(raf, raf.getChannel().tryLock());
      raf.getChannel().truncate(0);
      RandomAccessFileLockProvider.closeRandomAccessFileIfLockNotAcquired(raf, null);
      Assertions.assertThatThrownBy(() -> {
        raf.getChannel().truncate(0);
      }).isInstanceOf(ClosedChannelException.class);
    }
  }
  
  @Test
  public void testTryLock() throws IOException {
    final File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "RandomAccessFileLockProviderTest");
    try (Closeable lock = RandomAccessFileLockProvider.getInstance().tryLock(file)) {
      assertNotNull(lock);
    }
  }
  
  @Test
  public void testGetInstance() {
    assertSame(RandomAccessFileLockProvider.getInstance(), RandomAccessFileLockProvider.getInstance());
  }
}
