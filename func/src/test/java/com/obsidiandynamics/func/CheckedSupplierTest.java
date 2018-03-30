package com.obsidiandynamics.func;

import static org.junit.Assert.*;

import org.junit.*;

public final class CheckedSupplierTest {
  @Test
  public void testGet() throws Exception {
    final ThrowingSupplier<String> s = () -> "test";
    assertEquals("test", s.get());
  }
  
  @Test
  public void testWrap() {
    final CheckedSupplier<String, RuntimeException> s = CheckedSupplier.wrap(() -> "test");
    assertEquals("test", s.get());
  }
}
