package com.obsidiandynamics.resolver;

import static org.junit.Assert.*;

import java.util.function.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class SingletonTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Singleton.class);
  }
  
  @Test
  public void testLazy() {
    final Supplier<String> singleton = Singleton.of(() -> new String("test"));
    final String s0 = singleton.get();
    final String s1 = singleton.get();
    assertEquals("test", s0);
    assertSame(s0, s1);
  }
  
  @Test
  public void testEager() {
    final Supplier<String> singleton = Singleton.of("test");
    final String s0 = singleton.get();
    final String s1 = singleton.get();
    assertEquals("test", s0);
    assertSame(s0, s1);
  }
}
