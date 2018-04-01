package com.obsidiandynamics.zlg;

import static org.junit.Assert.*;

import org.junit.*;
import org.junit.runner.*;

import com.obsidiandynamics.testmark.*;

public final class ZlgDisabledVolumeTest extends AbstractDisabledVolumeTest {
  @Test
  public void test() {
    runBenchmark(null, TEST_RUN_TIME_NANOS, cycle());
  }
  
  @Test
  public void testBenchmark() {
    final String name = "Zlg";
    Testmark.ifEnabled(name, () -> runBenchmark(name, BENCHMARK_RUN_TIME_NANOS, cycle()));
  }

  private static TestCycle cycle() {
    final Zlg z = Zlg.forClass(AbstractDisabledVolumeTest.class).get();
    assertFalse(z.isEnabled(LogLevel.TRACE));
    
    return (f, d, i, l) -> {
      consumeArgs(f, d, i, l);
      z.t("float: %f, double: %f, int: %d, long: %d").arg(f).arg(d).arg(i).arg(l).log();
    };
  }
  
  public static void main(String[] args) {
    Testmark.enable();
    JUnitCore.runClasses(ZlgDisabledVolumeTest.class);
  }
}
