package com.obsidiandynamics.func;

import static org.junit.Assert.*;

import java.util.function.*;

import org.junit.*;

public final class CheckedFunctionTest {
  private static int timesTwo(int input) {
    return input * 2;
  }

  @Test
  public void testApply() throws Throwable {
    final ThrowingFunction<String, Integer> f0 = Integer::parseInt;
    final CheckedFunction<String, Integer, Throwable> f1 = f0.andThen(CheckedFunctionTest::timesTwo);
    final CheckedFunction<byte[], Integer, Throwable> f2 = f1.compose(String::new);
    assertEquals(20, (int) f2.apply("10".getBytes()));
  }

  @Test
  public void testToChecked() {
    final CheckedFunction<String, Integer, RuntimeException> f = CheckedFunction.toChecked(Integer::parseInt);
    assertEquals(10, (int) f.apply("10"));
  }

  @Test
  public void testToUnchecked() {
    final Function<String, Integer> f = CheckedFunction.toUnchecked(Integer::parseInt);
    assertEquals(10, (int) f.apply("10"));
  }
  
  @Test
  public void testIdentity() {
    final CheckedFunction<String, String, RuntimeException> identity = CheckedFunction.identity();
    final String arg = "foo";
    assertSame(arg, identity.apply(arg));
  }
}
