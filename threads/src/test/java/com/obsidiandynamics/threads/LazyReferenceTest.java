package com.obsidiandynamics.threads;

import static com.obsidiandynamics.func.Functions.*;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.*;

import org.junit.*;

import com.obsidiandynamics.await.*;

public final class LazyReferenceTest {
  @Test
  public void testGetSingleCheck() {
    final LazyReference<String, RuntimeException> ref = LazyReference.from(give("str"));
    assertNull(ref.peek());
    assertTrue(ref.toString().contains("null"));

    final String value = ref.get();
    assertEquals("str", value);
    assertSame(value, ref.get());
    assertSame(value, ref.peek());
    assertTrue(ref.toString().contains("str"));
  }

  @Test
  public void testGetDoubleCheck() {
    final AtomicReference<LazyReference<String, RuntimeException>> refPtr = new AtomicReference<>();
    final LazyReference<String, RuntimeException> ref = LazyReference.from(() -> {
      final Thread losingThread = new Thread(() -> {
        refPtr.get().get();
      });
      losingThread.start();
      Timesert.wait(10_000).untilTrue(() -> losingThread.getState() == Thread.State.BLOCKED);
      return "str";
    });
    refPtr.set(ref);

    final String value = ref.get();
    assertEquals("str", value);
    assertSame(value, ref.get());
    assertSame(value, ref.peek());
    assertTrue(ref.toString().contains("str"));
  }
}
