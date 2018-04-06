package com.obsidiandynamics.dyno;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class BenchmarkResultTest {
  @Test
  public void test() {
    final BenchmarkResult result = new BenchmarkResult(100, 3.14, "result");
    assertEquals(100, result.getDuration());
    assertEquals(3.14, result.getPrimaryScore(), Double.MIN_VALUE);
    assertEquals("result", result.getResult());
    
    Assertions.assertToStringOverride(result);
  }
}
