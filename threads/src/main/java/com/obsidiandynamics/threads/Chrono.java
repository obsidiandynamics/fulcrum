package com.obsidiandynamics.threads;

import static com.obsidiandynamics.func.Functions.*;

/**
 *  High precision parking behaviour for threads. Chrono can park a thread to a level
 *  of granularity that isn't possible with {@link Thread#sleep}. <p>
 *  
 *  Chrono works by cascading three types of blocking behaviour — sleeping, yielding
 *  and spin-waiting — depending on the proximity of the wake time. The
 *  {@link #parkNanos(long)} method can switch between all three strategies in a 
 *  single invocation, starting with the
 *  least granular (but the most CPU-efficient), progressing through to the most granular
 *  (and least CPU-efficient) as the wake time nears, using configured thresholds as a
 *  way of selecting the appropriate blocking strategy. Custom thresholds can be passed as
 *  arguments to the constructor. Alternatively, the default granularity
 *  thresholds have shown good results on Linux and macOS (x86-64). (Windows OS hasn't 
 *  been tested, but it is expected that the thresholds will need to be increased 
 *  significantly due to the coarse-grained nature of the Windows scheduler.) <p>
 *  
 *  Using Chrono, it is possible to get the blocking granularity down to about a microsecond,
 *  which is three orders of magnitude better than the {@link Thread#sleep}
 *  alternative. This technique also has the effect of reducing machine-specific 
 *  variations. Chrono can be used as a general purpose {@link Thread#sleep} replacement;
 *  however, it is not as CPU-efficient, on average taking up more CPU time and causing 
 *  more frequent thread state changes. Consequently, use Chrono only where consistent 
 *  sub-millisecond precision is required. <p>
 *  
 *  This class is thread-safe.
 */
public final class Chrono {
  public static final long NANOS_IN_MICROSECOND = 1_000L;
  
  public static final long NANOS_IN_MILLISECOND = 1_000_000L;
  
  public static final long NANOS_IN_SECOND = 1_000_000_000L;
  
  private static final long DEF_SLEEP_GRANULARITY = 5 * NANOS_IN_MILLISECOND;
  
  private static final long DEF_YIELD_GRANULARITY = 5 * NANOS_IN_MICROSECOND;
  
  private static final Chrono DEFAULT = new Chrono(DEF_SLEEP_GRANULARITY, DEF_YIELD_GRANULARITY);
  
  /**
   *  Obtains the default {@link Chrono} instance, configured with sleep and yield granularity
   *  thresholds suitable for Linux and macOS.
   *  
   *  @return The default {@link Chrono} instance.
   */
  public static Chrono getDefault() {
    return DEFAULT;
  }
  
  private final long sleepGranularity;
  
  private final long yieldGranularity;
  
  /**
   *  Constructor.
   *  
   *  @param sleepGranularityNanos The sleep threshold; a sleep state will not be entered unless the
   *                               remaining time is less than {@code sleepGranularityNanos * 2}.
   *  @param yieldGranularityNanos The yield threshold; a yield will not be issued unless the
   *                               remaining time is less than {@code yieldGranularityNanos * 2}.
   */
  public Chrono(long sleepGranularityNanos, long yieldGranularityNanos) {
    this.sleepGranularity = sleepGranularityNanos;
    this.yieldGranularity = yieldGranularityNanos;
  }
  
  /**
   *  Obtains the configured sleep granularity.
   *  
   *  @return The sleep granularity (in nanoseconds).
   */
  public long getSleepGranularity() {
    return sleepGranularity;
  }

  /**
   *  Obtains the configured yield granularity. <p>
   *  
   *  No blocking occurs if the provided value is zero or negative.
   *  
   *  @return The yield granularity (in nanoseconds).
   */
  public long getYieldGranularity() {
    return yieldGranularity;
  }

  /**
   *  Blocks the caller for a specified number of seconds. <p>
   *  
   *  No blocking occurs if the provided value is zero or negative.
   *  
   *  @param seconds The number of seconds to block for.
   *  @throws InterruptedException If the thread was interrupted.
   */
  public void parkSeconds(double seconds) throws InterruptedException {
    parkSeconds(seconds, sleepGranularity, yieldGranularity);
  }
  
  /**
   *  Blocks the caller for a specified number of nanoseconds.
   *  
   *  @param nanos The number of nanoseconds to block for.
   *  @throws InterruptedException If the thread was interrupted.
   */
  public void parkNanos(long nanos) throws InterruptedException {
    parkNanos(nanos, sleepGranularity, yieldGranularity);
  }

  public static void parkSeconds(double seconds, long sleepGranularityNanos, long yieldGranularityNanos) throws InterruptedException {
    parkNanos((long) (seconds * NANOS_IN_SECOND), sleepGranularityNanos, yieldGranularityNanos);
  }
  
  public static void parkNanos(long nanos, long sleepGranularityNanos, long yieldGranularityNanos) throws InterruptedException {
    final long wakeTime = System.nanoTime() + nanos;
    for (;;) {
      mustBeFalse(Thread.interrupted(), InterruptedException::new);
      final long remainingNanos = wakeTime - System.nanoTime();
      if (remainingNanos >= 2 * sleepGranularityNanos) {
        // coarse-grained — sleep
        final long sleepMillis = round(remainingNanos - sleepGranularityNanos, sleepGranularityNanos) / NANOS_IN_MILLISECOND;
        //noinspection BusyWait
        Thread.sleep(sleepMillis);
      } else if (remainingNanos >= 2 * yieldGranularityNanos) {
        // mid-grained — yield
        Thread.yield();
      } else if (remainingNanos > 0) {
        // fine-grained — busy wait
      } else {
        break;
      }
    }
  }
  
  private static long round(long amount, long mod) {
    final long remainder = amount % mod;
    return amount - remainder;
  }

  @Override
  public String toString() {
    return Chrono.class.getSimpleName() + " [sleepGranularity=" + sleepGranularity + ", yieldGranularity=" + yieldGranularity + "]";
  }
}
