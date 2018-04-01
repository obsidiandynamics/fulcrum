package com.obsidiandynamics.zlg;

import org.junit.*;
import org.junit.runner.*;

import com.obsidiandynamics.testmark.*;

public final class BaselineDisabledVolumeTest extends AbstractDisabledVolumeTest {
  @Test
  public void test() {
    runBenchmark(null, TEST_RUN_TIME_NANOS, cycle());
  }

  @Test
  public void testBenchmark() {
    final String name = "Baseline";
    Testmark.ifEnabled(name, () -> runBenchmark(name, BENCHMARK_RUN_TIME_NANOS, cycle()));
  }
  
  private static TestCycle cycle() {
    return (f, d, i, l) -> {
      consumeArgs(f, d, i, l);
    };
  }
  
  public static void main(String[] args) {
    Testmark.enable();
    JUnitCore.runClasses(BaselineDisabledVolumeTest.class);
  }
}
