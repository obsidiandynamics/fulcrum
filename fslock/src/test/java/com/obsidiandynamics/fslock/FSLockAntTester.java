package com.obsidiandynamics.fslock;

public final class FSLockAntTester {
  public static void main(String[] args) throws InterruptedException {
    final Thread t0 = forkInNewThread();
    final Thread t1 = forkInNewThread();
    t0.join();
    t1.join();
  }
  
  private static Thread forkInNewThread() {
    final Thread thread = new Thread(new AntFork(FSLockTester.class)::run);
    thread.start();
    return thread;
  }
}
