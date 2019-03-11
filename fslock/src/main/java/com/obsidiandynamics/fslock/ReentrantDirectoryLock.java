package com.obsidiandynamics.fslock;

import java.io.*;

/**
 *  Ensures that a single reentrant acquisition of a {@link DirectoryLock} may only be released at 
 *  most once, and can be used from a try-with-resources block.
 */
public final class ReentrantDirectoryLock implements Closeable {
  private final DirectoryLock directoryLock;
  
  private boolean released;
  
  ReentrantDirectoryLock(DirectoryLock nodeLock) {
    this.directoryLock = nodeLock;
  }
  
  /**
   *  Releases this lock, assuming that the current thread is the lock owner. (Otherwise
   *  an {@link IllegalStateException} is thrown.)
   *  
   *  @throws IOException If an I/O error occurs.
   */
  public void release() throws IOException {
    release(true);
  }
  
  /**
   *  Releases this lock. This can only be done once per acquisition (subsequent calls are
   *  reduced to a no-op). <p>
   *  
   *  Set {@code threadIsOwner} to {@code false} to bypass the thread ownership check.
   *  
   *  @param threadIsOwner Ensures that the current thread is the owner before releasing; 
   *                       an {@link IllegalStateException} is thrown otherwise.
   *  @throws IOException If an I/O error occurs.
   */
  public void release(boolean threadIsOwner) throws IOException {
    if (! released) {
      directoryLock.leave(threadIsOwner);
      released = true;
    }
  }
  
  /**
   *  Closes this lock, delegating to {@link #release()}.
   */
  @Override
  public void close() throws IOException {
    release();
  }
  
  public DirectoryLock getLock() {
    return directoryLock;
  }
  
  @Override
  public String toString() {
    return ReentrantDirectoryLock.class.getSimpleName() + " [" + directoryLock.baseToString() + "]";
  }
}
