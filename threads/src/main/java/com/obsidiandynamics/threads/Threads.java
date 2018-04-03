package com.obsidiandynamics.threads;

import java.util.concurrent.*;

import com.obsidiandynamics.func.*;

public final class Threads {
  private Threads() {}

  public static boolean await(CountDownLatch latch) {
    return runUninterruptedly(latch::await);
  }

  public static boolean await(CyclicBarrier barrier) {
    return runUninterruptedly(() -> Exceptions.wrap((CheckedRunnable<?>) barrier::await, 
                                                    IllegalStateException::new));
  }

  public static boolean sleep(long millis) {
    if (millis > 0) {
      return runUninterruptedly(() -> Thread.sleep(millis));
    } else {
      return ! Thread.currentThread().isInterrupted();
    }
  }

  public static boolean runUninterruptedly(CheckedRunnable<InterruptedException> interruptible) {
    try {
      interruptible.run();
      return true;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }
  
  public static <X extends Throwable> long tookMillis(CheckedRunnable<X> r) throws X {
    return tookNanos(r) / 1_000_000L;
  }
  
  public static <X extends Throwable> long tookNanos(CheckedRunnable<X> r) throws X {
    final long started = System.nanoTime();
    r.run();
    return System.nanoTime() - started;
  }
}
