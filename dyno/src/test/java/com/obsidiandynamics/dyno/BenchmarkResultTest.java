package com.obsidiandynamics.dyno;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class BenchmarkResultTest {
  @Test
  public void testFields() {
    final BenchmarkResult result = new BenchmarkResult(100, 3.14, "result");
    assertEquals(100, result.getDuration());
    assertEquals(3.14, result.getScore(), Double.MIN_VALUE);
    assertEquals("result", result.getResult());
  }
  
  @Test
  public void testToStringWithResult() {
    Assertions.assertToStringOverride(new BenchmarkResult(100, 3.14, "result"));
  }
  
  @Test
  public void testToStringWithoutResult() {
    Assertions.assertToStringOverride(new BenchmarkResult(100, 3.14, null));
  }
}
