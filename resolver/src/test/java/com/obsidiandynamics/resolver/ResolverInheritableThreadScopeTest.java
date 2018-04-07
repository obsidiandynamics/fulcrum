package com.obsidiandynamics.resolver;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.junit.*;

import com.obsidiandynamics.resolver.Resolver.*;

public final class ResolverInheritableThreadScopeTest {
  private static ScopedResolver scoped() {
    return Resolver.scoped(Scope.INHERITABLE_THREAD);
  }
  
  @After
  public void after() {
    scoped().reset();
  }
  
  @Test
  public void testWithDefaultValueSupplier() throws InterruptedException {
    scoped().assign(String.class, Singleton.of("assigned"));
    assertEquals("assigned", scoped().lookup(String.class, () -> "unassigned").get());
    
    // verify that the value can be read by a child thread
    final AtomicReference<String> childValue = new AtomicReference<>();
    final Thread childThread = new Thread(() -> {
      childValue.set(scoped().lookup(String.class, () -> "unassigned").get());
    });
    childThread.start();
    childThread.join();
    assertEquals("assigned", childValue.get());
    
    scoped().reset(String.class);
    assertEquals("default", scoped().lookup(String.class, () -> "default").get());
  }
  
  @Test
  public void testResetAll() {
    scoped().assign(String.class, Singleton.of("assigned"));
    assertEquals("assigned", scoped().lookup(String.class).get());
    
    scoped().reset();
    assertNull(scoped().lookup(String.class).get());
  }
  
  @Test
  public void testNullSupplier() {
    final Supplier<String> stringSupplier = scoped().lookup(String.class);
    assertNotNull(stringSupplier);
    assertNull(stringSupplier.get());
  }
}
