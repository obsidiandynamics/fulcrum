package com.obsidiandynamics.resolver;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class ResolverThreadScopeTest {
  @After
  public void after() {
    Resolver.reset();
  }
  
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Resolver.class);
  }

  @Test
  public void testWithDefaultValueSupplier() throws InterruptedException {
    Resolver.assign(String.class, Singleton.of("assigned"));
    assertEquals("assigned", Resolver.lookup(String.class, () -> "unassigned").get());
    
    // verify that the value can't be read by another thread
    final AtomicReference<String> other = new AtomicReference<>();
    final Thread otherThread = new Thread(() -> {
      other.set(Resolver.lookup(String.class, () -> "unassigned").get());
    });
    otherThread.start();
    otherThread.join();
    assertEquals("unassigned", other.get());
    
    Resolver.reset(String.class);
    assertEquals("default", Resolver.lookup(String.class, () -> "default").get());
  }
  
  @Test
  public void testResetAll() {
    Resolver.assign(String.class, Singleton.of("assigned"));
    assertEquals("assigned", Resolver.lookup(String.class).get());
    
    Resolver.reset();
    assertNull(Resolver.lookup(String.class).get());
  }
  
  @Test
  public void testNullSupplier() {
    final Supplier<String> stringSupplier = Resolver.lookup(String.class);
    assertNotNull(stringSupplier);
    assertNull(stringSupplier.get());
  }
  
  @Test
  public void testDefaultScope() {
    assertEquals(Scope.THREAD, Resolver.getDefaultScope());
  }
}
