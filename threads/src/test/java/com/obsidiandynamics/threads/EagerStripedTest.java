package com.obsidiandynamics.threads;

import static org.junit.Assert.*;

import org.junit.*;

public final class EagerStripedTest {
  @Test
  public void testGet_differentKeys() {
    final EagerStriped<Object> striped = new EagerStriped<>(16, Object::new);
    assertSame(striped.get(15), striped.get(15));
    assertSame(striped.get(16), striped.get(16));
    assertNotSame(striped.get(15), striped.get(16));
  }
}
