package com.obsidiandynamics.fslock;

import java.io.*;
import java.nio.channels.*;

/**
 *  A {@link LockProvider} implementation based on a {@link RandomAccessFile}. This is the
 *  default implementation of a {@link LockProvider}.
 */
public final class RandomAccessFileLockProvider implements LockProvider {
  private static final RandomAccessFileLockProvider INSTANCE = new RandomAccessFileLockProvider();
  
  public static RandomAccessFileLockProvider getInstance() { return INSTANCE; }
  
  private RandomAccessFileLockProvider() {}
  
  @Override
  public Closeable tryLock(File file) throws IOException {
    final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
    FileLock lock = null;
    try {
      lock = randomAccessFile.getChannel().tryLock();
      return randomAccessFileIfLockAcquired(randomAccessFile, lock);
    } finally {
      closeRandomAccessFileIfLockNotAcquired(randomAccessFile, lock);
    }
  }
  
  static RandomAccessFile randomAccessFileIfLockAcquired(RandomAccessFile randomAccessFile, FileLock lock) {
    return lock != null ? randomAccessFile : null;
  }
  
  static void closeRandomAccessFileIfLockNotAcquired(RandomAccessFile randomAccessFile, FileLock lock) throws IOException {
    if (lock == null) randomAccessFile.close();
  }
}
