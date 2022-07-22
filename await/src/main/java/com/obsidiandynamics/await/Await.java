package com.obsidiandynamics.await;

import java.util.concurrent.*;
import java.util.function.*;

/**
 *  Utility for awaiting a specific condition. The utility methods block the calling thread until a 
 *  certain condition, described by the specified {@link BooleanSupplier} evaluates to {@code true}.<p>
 *  
 *  There are variations of the blocking methods - some return a {@code boolean}, indicating whether 
 *  the condition has been satisfied within the allotted time frame, while others throw a 
 *  {@link TimeoutException}. The caller can specify an upper bound on how long to wait for, as well as the 
 *  checking interval (which otherwise defaults to 1 ms). All times are in milliseconds.
 */
public final class Await {
  /** The default check interval. */
  public static final int DEF_INTERVAL = 1;
  
  /** 
   *  The condition will be evaluated at least once, even if the deadline had passed prior to
   *  when {@code bounded()} or {@code boundedTimeout()} was invoked.
   */
  public static final boolean AT_LEAST_ONCE = true;
  
  /** 
   *  The condition might never be evaluated, if the deadline had passed before {@code bounded()}
   *  or {@code boundedTimeout()} was invoked.
   */
  public static final boolean POSSIBLY_NEVER = false;
  
  private Await() {}
  
  /**
   *  A variant of {@link #perpetual(int, BooleanSupplier)}, using an interval of 
   *  {@link #DEF_INTERVAL} between successive tests.
   *  
   *  @param condition The condition to await.
   *  @throws InterruptedException If the thread was interrupted while waiting for the condition.
   */
  public static void perpetual(BooleanSupplier condition) throws InterruptedException {
    bounded(Integer.MAX_VALUE, DEF_INTERVAL, condition);
  }
  
  /**
   *  Blocks indefinitely until the condition specified by the given {@link BooleanSupplier}
   *  is satisfied.
   *  
   *  @param intervalMillis The interval between successive tests, in milliseconds.
   *  @param condition The condition to await.
   *  @throws InterruptedException If the thread was interrupted while waiting for the condition.
   */
  public static void perpetual(int intervalMillis, BooleanSupplier condition) throws InterruptedException {
    bounded(Integer.MAX_VALUE, intervalMillis, condition);
  }
  
  /**
   *  A variant of {@link #boundedTimeout(int, int, BooleanSupplier)}, using an interval of 
   *  {@link #DEF_INTERVAL} between successive tests.
   *  
   *  @param waitMillis The upper bound on the wait time, in milliseconds.
   *  @param condition The condition to await.
   *  @throws InterruptedException If the thread was interrupted while waiting for the condition.
   *  @throws TimeoutException If the condition wasn't satisfied within the given time frame.
   */
  public static void boundedTimeout(int waitMillis, BooleanSupplier condition) throws InterruptedException, TimeoutException {
    boundedTimeout(waitMillis, DEF_INTERVAL, condition);
  }
  
  /**
   *  Awaits a condition specified by the given {@link BooleanSupplier}, blocking until the condition
   *  evaluates to {@code true}. If the condition isn't satisfied within the alloted time frame, a 
   *  {@link TimeoutException} is thrown. <p>
   *  
   *  This variant will use the current time as the starting point for the timeout, and will always
   *  evaluate the condition at least once.
   *  
   *  @param waitMillis The upper bound on the wait time, in milliseconds.
   *  @param intervalMillis The interval between successive tests, in milliseconds.
   *  @param condition The condition to await.
   *  @throws InterruptedException If the thread was interrupted while waiting for the condition.
   *  @throws TimeoutException If the condition wasn't satisfied within the given time frame.
   */
  public static void boundedTimeout(int waitMillis, 
                                    int intervalMillis, 
                                    BooleanSupplier condition) throws InterruptedException, TimeoutException {
    boundedTimeout(System.currentTimeMillis(), waitMillis, intervalMillis, AT_LEAST_ONCE, condition);
  }
  
  /**
   *  Awaits a condition specified by the given {@link BooleanSupplier}, blocking until the condition
   *  evaluates to {@code true}. If the condition isn't satisfied within the alloted time frame, a 
   *  {@link TimeoutException} is thrown.
   *  
   *  @param startTime The wall clock time when the run was started, as reported by {@link System#currentTimeMillis()}.
   *  @param waitMillis The upper bound on the wait time, in milliseconds.
   *  @param intervalMillis The interval between successive tests, in milliseconds.
   *  @param atLeastOnce Set to true if the condition should be evaluated at least once, even if the deadline has passed.
   *  @param condition The condition to await.
   *  @throws InterruptedException If the thread was interrupted while waiting for the condition.
   *  @throws TimeoutException If the condition wasn't satisfied within the given time frame.
   */
  public static void boundedTimeout(long startTime,
                                    int waitMillis, 
                                    int intervalMillis, 
                                    boolean atLeastOnce,
                                    BooleanSupplier condition) throws InterruptedException, TimeoutException {
    if (! bounded(startTime, waitMillis, intervalMillis, atLeastOnce, condition)) {
      throw new TimeoutException(String.format("Timed out after %,d ms", System.currentTimeMillis() - startTime));
    }
  }
  
  /**
   *  A variant of {@link #bounded(int, int, BooleanSupplier)}, using an interval of 
   *  {@link #DEF_INTERVAL} between successive tests. <p>
   *  
   *  This variant will use the current time as the starting point for the timeout, and will always
   *  evaluate the condition at least once.
   *  
   *  @param waitMillis The upper bound on the wait time, in milliseconds.
   *  @param condition The condition to await.
   *  @return The final result of the tested condition; if {@code false} then this invocation has timed out.
   *  @throws InterruptedException If the thread was interrupted while waiting for the condition.
   */
  public static boolean bounded(int waitMillis, BooleanSupplier condition) throws InterruptedException {
    return bounded(waitMillis, DEF_INTERVAL, condition);
  }
  
  /**
   *  Awaits a condition specified by the given {@link BooleanSupplier}, blocking until the condition
   *  evaluates to {@code true}. <p>
   *  
   *  This variant will use the current time as the starting point for the timeout, and will always
   *  evaluate the condition at least once.
   *  
   *  @param waitMillis The upper bound on the wait time, in milliseconds.
   *  @param intervalMillis The interval between successive tests, in milliseconds.
   *  @param condition The condition to await.
   *  @return The final result of the tested condition; if {@code false} then this invocation has timed out.
   *  @throws InterruptedException If the thread was interrupted while waiting for the condition.
   */
  public static boolean bounded(int waitMillis, int intervalMillis, BooleanSupplier condition) throws InterruptedException {
    return bounded(System.currentTimeMillis(), waitMillis, intervalMillis, AT_LEAST_ONCE, condition);
  }
  
  /**
   *  Awaits a condition specified by the given {@link BooleanSupplier}, blocking until the condition
   *  evaluates to {@code true}.
   *  
   *  @param startTime The wall clock time when the run was started, as reported by {@link System#currentTimeMillis()}.
   *  @param waitMillis The upper bound on the wait time, in milliseconds.
   *  @param intervalMillis The interval between successive tests, in milliseconds.
   *  @param atLeastOnce Set to true if the condition should be evaluated at least once, even if the deadline has passed.
   *  @param condition The condition to await.
   *  @return The final result of the tested condition; if {@code false} then this invocation has timed out.
   *  @throws InterruptedException If the thread was interrupted while waiting for the condition.
   */
  public static boolean bounded(long startTime, int waitMillis, int intervalMillis, boolean atLeastOnce, BooleanSupplier condition) throws InterruptedException {
    final long deadline = startTime + waitMillis;
    try {
      if (! atLeastOnce && System.currentTimeMillis() > deadline) {
        return false;
      }
      
      for (;;) {
        final boolean result = condition.getAsBoolean();
        if (result) {
          return true;
        } else {
          final long remainingTime = deadline - System.currentTimeMillis();
          if (remainingTime > 0) {
            final long sleepTime = Math.min(remainingTime, intervalMillis);
            //noinspection BusyWait
            Thread.sleep(sleepTime);
          } else {
            return false;
          }
        }
      }
    } finally {
      if (Thread.interrupted()) {
        //noinspection ThrowFromFinallyBlock
        throw new InterruptedException("Wait interrupted");
      }
    }
  }
}
