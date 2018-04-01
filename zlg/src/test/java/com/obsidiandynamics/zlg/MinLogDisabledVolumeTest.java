package com.obsidiandynamics.zlg;

import static org.junit.Assert.*;

import org.junit.*;
import org.junit.runner.*;

import com.esotericsoftware.minlog.*;
import com.obsidiandynamics.testmark.*;

public final class MinLogDisabledVolumeTest extends AbstractDisabledVolumeTest {
  @Test
  public void testBenchmark() {
    final String name = "MinLog";
    Testmark.ifEnabled(name, () -> runBenchmark(name, BENCHMARK_RUN_TIME_NANOS, cycle()));
  }

  private static TestCycle cycle() {
    Log.set(Log.LEVEL_DEBUG);
    assertFalse(Log.TRACE);
    
    return (f, d, i, l) -> {
      if (l < 0) throw new AssertionError();
      Log.trace(String.format("float: %f, double: %f, int: %d, long: %d", f, d, i, l));
    };
  }
  
  public static void main(String[] args) {
    Testmark.enable();
    JUnitCore.runClasses(MinLogDisabledVolumeTest.class);
  }
}
