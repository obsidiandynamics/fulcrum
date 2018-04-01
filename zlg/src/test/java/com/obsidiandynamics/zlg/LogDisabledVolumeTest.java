package com.obsidiandynamics.zlg;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import com.obsidiandynamics.testmark.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class LogDisabledVolumeTest {
  private static final long TEST_RUN_TIME_NANOS = 10_000_000L;
  private static final long BENCHMARK_RUN_TIME_NANOS = 10_000_000_000L;
  
  @FunctionalInterface
  private interface TestCycle {
    void cycle();
  }

  @Test
  public void testBaseline() {
    runBenchmark(null, TEST_RUN_TIME_NANOS, nopCycle());
  }

  @Test
  public void testBaselineBenchmark() {
    Testmark.ifEnabled(() -> runBenchmark("Baseline", BENCHMARK_RUN_TIME_NANOS, nopCycle()));
  }
  
  @Test
  public void testZlg() {
    runBenchmark(null, TEST_RUN_TIME_NANOS, zlgCycle());
  }
  
  @Test
  public void testZlgBenchmark() {
    Testmark.ifEnabled(() -> runBenchmark("Zlg", BENCHMARK_RUN_TIME_NANOS, zlgCycle()));
  }
  
  private static void runBenchmark(String name, long runTimeNanos, TestCycle cycle) {
    final long checkRuns = 1_000_000L;
    final long started = System.nanoTime();
    
    long cycles = 0;
    long took;
    for (;;) {
      cycle.cycle();
      cycles++;
      if (cycles % checkRuns == 0) {
        took = System.nanoTime() - started;
        if (took > runTimeNanos) break;
      }
    }
    
    if (name != null) {
      final double perCycle = (double) took / cycles;
      System.out.format("%-8s: %,d cycles took %,d ns, %,.1f ns/cycle\n", name, cycles, took, perCycle);
    }
  }
  
  private static TestCycle nopCycle() {
    return () -> {};
  }

  private static TestCycle zlgCycle() {
    final Zlg z = Zlg.forClass(LogDisabledVolumeTest.class).get();
    return () -> {
      z.t("Int %d, float %f, boolean %b, long %d").arg(100).arg(0.5).arg(false).arg(400L).log();
    };
  }
  
  public static void main(String[] args) {
    Testmark.enable();
    JUnitCore.runClasses(LogDisabledVolumeTest.class);
  }
}
