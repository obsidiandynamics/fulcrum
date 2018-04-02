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
    return runUninterruptedly(() -> wrap((CheckedRunnable<?>) barrier::await, 
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
  
  @FunctionalInterface
  public interface ExceptionWrapper<X extends Throwable> extends Function<Throwable, X> {}
  
  public static <X extends Throwable> void wrap(CheckedRunnable<?> runnable, ExceptionWrapper<X> wrapper) throws X {
    wrap(() -> {
      runnable.run();
      return null;
    }, wrapper);
  }
  
  public static <T, X extends Throwable> T wrap(CheckedSupplier<? extends T, ?> supplier, ExceptionWrapper<X> wrapper) throws X {
    try {
      return supplier.get();
    } catch (Throwable e) {
      throw wrapper.apply(e);
    }
  }
}
