package com.obsidiandynamics.resolver;

import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.junit.*;

import com.obsidiandynamics.resolver.Resolver.*;

public final class ResolverThreadGroupScopeTest {
  private static ScopedResolver scoped() {
    return Resolver.scoped(Scope.THREAD_GROUP);
  }
  
  @Test
  public void testWithDefaultValueSupplier() throws InterruptedException {
    final AtomicReference<Throwable> error = new AtomicReference<>();
    
    final Thread parent = new Thread(new ThreadGroup(UUID.randomUUID().toString()), () -> {
      try {
        scoped().assign(String.class, Singleton.of("assigned"));
        assertEquals("assigned", scoped().lookup(String.class, () -> "unassigned").get());
        
        // verify that the value can be read by a child thread (with the same group)
        final AtomicReference<String> sameGroupValue = new AtomicReference<>();
        final Thread sameGroupThread = new Thread(() -> {
          sameGroupValue.set(scoped().lookup(String.class, () -> "unassigned").get());
        });
        sameGroupThread.start();
        sameGroupThread.join();
        assertEquals("assigned", sameGroupValue.get());
        

        // verify that the value can't be read by a thread with a different group
        final AtomicReference<String> diffGroupValue = new AtomicReference<>();
        final Thread diffGroupThread = new Thread(new ThreadGroup(UUID.randomUUID().toString()), () -> {
          diffGroupValue.set(scoped().lookup(String.class, () -> "unassigned").get());
        });
        diffGroupThread.start();
        diffGroupThread.join();
        assertEquals("unassigned", diffGroupValue.get());
        
        scoped().reset(String.class);
        assertEquals("default", scoped().lookup(String.class, () -> "default").get());
      } catch (Throwable e) {
        error.set(e);
      } finally {
        scoped().reset();
      }
    }, "parent");
    parent.start();
    parent.join();
    
    if (error.get() != null) throw new AssertionError(error.get().getMessage(), error.get());
  }
}