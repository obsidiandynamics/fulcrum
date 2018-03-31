package com.obsidiandynamics.resolver;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class ResolverTest {
  @After
  public void after() {
    Resolver.reset();
  }
  
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Resolver.class);
  }

  @Test
  public void test() {
    Resolver.assign(String.class, Singleton.of("assigned"));
    assertEquals("assigned", Resolver.lookup(String.class, () -> "assigned").get());
    
    Resolver.reset(String.class);
    assertEquals("default", Resolver.lookup(String.class, () -> "default").get());
  }
}
