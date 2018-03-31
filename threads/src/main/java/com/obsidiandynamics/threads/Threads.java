package com.obsidiandynamics.threads;

import java.util.concurrent.*;
import java.util.function.*;

import com.obsidiandynamics.func.*;

public final class Threads {
  private Threads() {}
  
  public static boolean await(CountDownLatch latch) {
    return runUninterruptedly(latch::await);
  }

  public static boolean await(CyclicBarrier barrier) {
    return runUninterruptedly(() -> wrapInRuntimeException(barrier::await, IllegalStateException::new));
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
  
  @FunctionalInterface
  public interface RuntimeExceptionMaker extends Function<Exception, RuntimeException> {}
  
  public static void wrapInRuntimeException(CheckedRunnable<?> runnable, RuntimeExceptionMaker exceptionMaker) {
    try {
      runnable.run();
    } catch (Exception e) {
      throw exceptionMaker.apply(e);
    }
  }
}
