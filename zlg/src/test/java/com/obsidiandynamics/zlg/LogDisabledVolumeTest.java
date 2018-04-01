package com.obsidiandynamics.zlg;

import static org.junit.Assert.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.slf4j.*;

import com.obsidiandynamics.testmark.*;

import squidpony.squidmath.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class LogDisabledVolumeTest {
  private static final long TEST_RUN_TIME_NANOS = 10_000_000L;
  private static final long BENCHMARK_RUN_TIME_NANOS = 10_000_000_000L;
  
  @FunctionalInterface
  private interface TestCycle {
    void cycle(float randomFloat, double randomDouble, int randomInt, long randomLong);
  }

  @Test
  public void testBaseline() {
    runBenchmark(null, TEST_RUN_TIME_NANOS, nopCycle());
  }

  @Test
  public void testBaselineBenchmark() {
    final String name = "Baseline";
    Testmark.ifEnabled(name, () -> runBenchmark(name, BENCHMARK_RUN_TIME_NANOS, nopCycle()));
  }
  
  @Test
  public void testSlf4j() {
    runBenchmark(null, TEST_RUN_TIME_NANOS, slf4jCycle());
  }
  
  @Test
  public void testSlf4jBenchmark() {
    final String name = "Slf4j";
    Testmark.ifEnabled(name, () -> runBenchmark(name, BENCHMARK_RUN_TIME_NANOS, slf4jCycle()));
  }
  
  @Test
  public void testZlg() {
    runBenchmark(null, TEST_RUN_TIME_NANOS, zlgCycle());
  }
  
  @Test
  public void testZlgBenchmark() {
    final String name = "Zlg";
    Testmark.ifEnabled(name, () -> runBenchmark(name, BENCHMARK_RUN_TIME_NANOS, zlgCycle()));
  }
  
  private static void runBenchmark(String name, long runTimeNanos, TestCycle cycle) {
    final long warmupTime = runTimeNanos / 2;
    final long checkRuns = 1_000_000L;
    boolean warmup = true;
    
    long cycles = 0;
    long took;
    final ThrustRNG random = new ThrustRNG();
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
      System.out.format("%-8s: %,d cycles took %,d ns, %,.1f ns/cycle\n", name, cycles, took, perCycle);
    }
  }
  
  private static TestCycle nopCycle() {
    return (f, d, i, l) -> {
      assert f >= 0;
      assert d >= 0;
      assert i >= 0;
      assert l >= 0;
    };
  }

  private static TestCycle zlgCycle() {
    final Zlg z = Zlg.forClass(LogDisabledVolumeTest.class).get();
    assertFalse(z.isEnabled(LogLevel.TRACE));
    
    return (f, d, i, l) -> {
      z.t("float: %f, double: %f, int: %d, long: %d").arg(f).arg(d).arg(i).arg(l).log();
    };
  }

  private static TestCycle slf4jCycle() {
    final Logger logger = LoggerFactory.getLogger(LogDisabledVolumeTest.class);
    assertFalse(logger.isTraceEnabled());
    
    return (f, d, i, l) -> {
      logger.trace("float: {}, double: {}, int: {}, long: {}", f, d, i, l);
    };
  }
  
  public static void main(String[] args) {
    Testmark.enable();
    JUnitCore.runClasses(LogDisabledVolumeTest.class);
  }
}
