package com.obsidiandynamics.func;

import static org.junit.Assert.*;

import org.junit.*;

public final class CheckedFunctionTest {
  private static int timesTwo(int input) {
    return input * 2;
  }
  
  @Test
  public void testApply() throws Exception {
    final ThrowingFunction<String, Integer> f0 = Integer::parseInt;
    final CheckedFunction<String, Integer, Exception> f1 = f0.andThen(CheckedFunctionTest::timesTwo);
    final CheckedFunction<byte[], Integer, Exception> f2 = f1.compose(String::new);
    assertEquals(20, (int) f2.apply("10".getBytes()));
  }
  
  @Test
  public void testWrap() {
    final CheckedFunction<String, Integer, RuntimeException> f = CheckedFunction.wrap(Integer::parseInt);
    assertEquals(10, (int) f.apply("10"));
  }
}
