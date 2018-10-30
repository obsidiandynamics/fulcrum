package com.obsidiandynamics.func;

import static org.junit.Assert.*;

import java.util.function.*;

import org.junit.*;

public final class CheckedSupplierTest {
  @Test
  public void testGet() throws Throwable {
    final ThrowingSupplier<String> s = () -> "test";
    assertEquals("test", s.get());
  }

  @Test
  public void testToChecked() {
    final CheckedSupplier<String, RuntimeException> s = CheckedSupplier.toChecked(() -> "test");
    assertEquals("test", s.get());
  }

  @Test
  public void testToUnchecked() {
    final Supplier<String> s = CheckedSupplier.toUnchecked(() -> "test");
    assertEquals("test", s.get());
  }
}
