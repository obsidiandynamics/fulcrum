package com.obsidiandynamics.zlg;

import org.junit.*;
import org.junit.runners.*;

import squidpony.squidmath.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractDisabledVolumeTest {
  protected static final long TEST_RUN_TIME_NANOS = 10_000_000L;
  protected static final long BENCHMARK_RUN_TIME_NANOS = 10_000_000_000L;
  
  @FunctionalInterface
  protected interface TestCycle {
    void cycle(float randomFloat, double randomDouble, int randomInt, long randomLong);
  }
  
  protected static void runBenchmark(String name, long runTimeNanos, TestCycle cycle) {
    final long warmupTime = runTimeNanos / 2;
    final long checkRuns = 1_000_000L;
    boolean warmup = true;
    
    long cycles = 0;
    long took;
    final RandomnessSource random = new ThrustRNG();
    if (name != null) System.out.print("Warming up... ");
    long started = System.nanoTime();
    for (;;) {
      // generate random numbers to avoid cache hits for boxed primitives
      final double randomDouble = RandomFP.toDouble(random.nextLong());
      final int randomInt = (int) (Integer.MAX_VALUE * randomDouble);
      final long randomLong = randomInt;
      final float randomFloat = (float) randomDouble;
      
      cycle.cycle(randomFloat, randomDouble, randomInt, randomLong);
      cycles++;
      if (cycles % checkRuns == 0) {
        took = System.nanoTime() - started;
        if (warmup) {
          if (took > warmupTime) {
            warmup = false;
            if (name != null) System.out.println("starting timed run... ");
            cycles = 0;
            started = System.nanoTime();
          }
        } else {
          if (took > runTimeNanos) {
            break;
          }
        }
      }
    }
    
    if (name != null) {
      final double perCycle = (double) took / cycles;
      final double tookMillis = took / 1_000_000d;
      System.out.format("%-10s: %,d cycles took %,.0f ms, %,.1f ns/cycle\n", name, cycles, tookMillis, perCycle);
    }
  }
}
