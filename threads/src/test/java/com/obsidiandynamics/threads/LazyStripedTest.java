package com.obsidiandynamics.threads;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.junit.*;

import com.obsidiandynamics.func.*;

public final class LazyStripedTest {
  @Test
  public void testGet_contended() {
    final Supplier<Object> valueSupplier = Classes.cast(mock(Supplier.class));
    final LazyStriped<Object> striped = new LazyStriped<>(16, valueSupplier);
    final AtomicInteger invocations = new AtomicInteger();
    final Object expectedValue = new Object();
    final int keyHash = 42;
    when(valueSupplier.get()).thenAnswer(__ -> {
      if (invocations.getAndIncrement() == 0) {
        striped.get(keyHash);
        return new Object(); // this value should not be used as the second invocation will preempt the first
      } else {
        return expectedValue;
      }
    });
    
    assertSame(expectedValue, striped.get(keyHash));
    verify(valueSupplier, times(2)).get();
    
    // obtaining the value a second time should not cause any further supplier invocations
    assertSame(expectedValue, striped.get(keyHash));
    verify(valueSupplier, times(2)).get();
  }
  
  @Test
  public void testGet_differentKeys() {
    final LazyStriped<Object> striped = new LazyStriped<>(16, Object::new);
    assertSame(striped.get(15), striped.get(15));
    assertSame(striped.get(16), striped.get(16));
    assertNotSame(striped.get(15), striped.get(16));
  }
}
