package com.obsidiandynamics.threads;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.verifier.*;

public final class ChronoTest {
  private static final boolean PRINT_STATS = false;

  @Test
  public void testPojo() {
    PojoVerifier.forClass(Chrono.class).verify();
  }

  @Test
  public void testParkSeconds() throws InterruptedException {
    final double targetSleepTime = 0.012_345_678;
    final long start = System.nanoTime();
    Chrono.getDefault().parkSeconds(targetSleepTime);
    final long slept = System.nanoTime() - start;
    assertTrue("slept=" + slept, slept >= targetSleepTime);
  }
  
  @Test
  public void testParkNanos_zero() throws InterruptedException {
    Chrono.getDefault().parkNanos(0);
  }
  
  @Test
  public void testParkNanos_negative() throws InterruptedException {
    Chrono.getDefault().parkNanos(-1);
  }

  @Test
  public void testParkNanos_withMeasurement() throws InterruptedException {
    final long targetSleepTime = 12_345_678L;
    final Chrono chrono = Chrono.getDefault();
    long totalSleepTime = 0L;
    final int cycles = 5;
    final int discardCycles = 1;
    for (int i = 0; i < cycles; i++) {
      final long start = System.nanoTime();
      chrono.parkNanos(targetSleepTime);
      final long slept = System.nanoTime() - start;
      assertTrue("slept=" + slept, slept >= targetSleepTime);
      if (i >= discardCycles) {
        totalSleepTime += slept;
      }
      if (PRINT_STATS) System.out.format("slept for %,d\n", slept);
    }
    final long averageSleepTime = totalSleepTime / (cycles - discardCycles);
    final long delta = averageSleepTime - targetSleepTime;
    if (PRINT_STATS) System.out.format("average: %,d\ntarget:  %,d\ndelta:   %,d\n", averageSleepTime, targetSleepTime, delta);
  }

  @Test
  public void testParkNanos_yieldAndBusyWaitOnly() throws InterruptedException {
    Chrono.parkNanos(50_000_000L, 1_000_000_000L, 1);
  }

  @Test
  public void testParkNanos_busyWaitOnly() throws InterruptedException {
    Chrono.parkNanos(10_000_000L, 1_000_000_000L, 1_000_000_000L);
  }

  @Test(expected=InterruptedException.class)
  public void testParkNanos_interrupted() throws InterruptedException {
    Thread.currentThread().interrupt();
    try {
      Chrono.getDefault().parkNanos(10_000_000_000L);
    } finally {
      assertFalse(Thread.interrupted());
    }
  }
}
