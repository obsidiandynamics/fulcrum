package com.obsidiandynamics.func;

import static org.junit.Assert.*;

import org.junit.*;

public final class NullArgumentExceptionTest {
  @Test
  public void testConstructor() {
    assertNull(new NullArgumentException().getMessage());
    assertEquals("message", new NullArgumentException("message").getMessage());
  }
}
