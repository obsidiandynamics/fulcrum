package com.obsidiandynamics.threads;

import java.util.concurrent.*;

import com.obsidiandynamics.func.*;

/**
 *  Utilities for working with threads, interrupts and various synchronisation primitives.
 */
public final class Threads {
  private Threads() {}

  /**
   *  Sleeps for the specified amount if the latter is a positive value, trapping an interrupt
   *  and deferring it by setting {@link Thread#interrupt()}.
   *  
   *  @param millis The number of milliseconds to sleep for.
   *  @return True if this method slept for the specified duration, or false if it was interrupted.
   */
  public static boolean sleep(long millis) {
    if (millis > 0) {
      return deferInterrupt(() -> Thread.sleep(millis));
    } else {
      // if millis <= 0, we still need to check if we were interrupted (but don't clear the status)
      return ! Thread.currentThread().isInterrupted();
    }
  }

  /**
   *  Runs a specified {@link CheckedRunnable}, trapping an interrupt
   *  and deferring it by setting {@link Thread#interrupt()}.
   *  
   *  @param interruptible The block to run.
   *  @return True if the block run uninterruptedly, or false if an interrupt was trapped.
   */
  public static boolean deferInterrupt(CheckedRunnable<InterruptedException> interruptible) {
    try {
      interruptible.run();
      return true;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }
  
  /**
   *  Runs a specified {@link CheckedBooleanSupplier} and returns the resulting value, trapping an interrupt
   *  and deferring it by setting {@link Thread#interrupt()}.
   *  
   *  @param interruptible The block to run.
   *  @param onInterrupt The value to return in the event of an interrupt.
   *  @return The resulting {@code boolean}.
   */
  public static boolean deferInterrupt(CheckedBooleanSupplier<InterruptedException> interruptible, boolean onInterrupt) {
    try {
      return interruptible.getAsBoolean();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return onInterrupt;
    }
  }
  
  /**
   *  Runs a specified {@link CheckedSupplier} and returns the resulting value, trapping an interrupt
   *  and deferring it by setting {@link Thread#interrupt()}.
   *  
   *  @param <T> Result type.
   *  @param interruptible The block to run.
   *  @param onInterrupt The value to return in the event of an interrupt.
   *  @return The resulting value.
   */
  public static <T> T deferInterrupt(CheckedSupplier<? extends T, InterruptedException> interruptible, T onInterrupt) {
    try {
      return interruptible.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return onInterrupt;
    }
  }

  /**
   *  Awaits a latch, trapping an interrupt and deferring it by setting {@link Thread#interrupt()}.
   *  
   *  @param latch The latch to await.
   *  @return True if the latch was successfully awaited, or false if an interrupt was trapped.
   */
  public static boolean await(CountDownLatch latch) {
    return deferInterrupt(latch::await);
  }
  
  /**
   *  A variant of a {@link RuntimeException} thrown in lieu of a {@link BrokenBarrierException}.
   */
  public static final class RuntimeBrokenBarrierException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    RuntimeBrokenBarrierException(Throwable cause) { super(cause); }
  }

  /**
   *  Awaits a latch, trapping an interrupt and deferring it by setting {@link Thread#interrupt()}.
   *  
   *  @param barrier The barrier to await.
   *  @return True if the barrier was successfully awaited, or false if an interrupt was trapped.
   *  @throws RuntimeBrokenBarrierException If the barrier was broken.
   */
  public static boolean await(CyclicBarrier barrier) {
    return deferInterrupt(() -> {
      try {
        barrier.await();
      } catch (BrokenBarrierException e) {
        throw new RuntimeBrokenBarrierException(e);
      }
    });
  }
  
  /**
   *  Executes a given {@link CheckedRunnable} block, returning the duration of the run
   *  in milliseconds.
   *  
   *  @param <X> Throwable type.
   *  @param r The runnable.
   *  @return The run duration, in milliseconds.
   *  @throws X If an exception occurs.
   */
  public static <X extends Throwable> long tookMillis(CheckedRunnable<X> r) throws X {
    return tookNanos(r) / 1_000_000L;
  }
  
  /**
   *  Executes a given {@link CheckedRunnable} block, returning the duration of the run
   *  in nanoseconds.
   *  
   *  @param <X> Throwable type.
   *  @param r The runnable.
   *  @return The run duration, in nanoseconds.
   *  @throws X If an exception occurs.
   */
  public static <X extends Throwable> long tookNanos(CheckedRunnable<X> r) throws X {
    final long started = System.nanoTime();
    r.run();
    return System.nanoTime() - started;
  }
}
