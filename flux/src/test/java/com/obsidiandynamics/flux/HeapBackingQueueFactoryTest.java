package com.obsidiandynamics.flux;

import static org.junit.Assert.*;

import org.junit.*;

public final class HeapBackingQueueFactoryTest {
  @Test
  public void testGetInstance() {
    assertSame(HeapBackingQueueFactory.getInstance(), HeapBackingQueueFactory.getInstance());
  }
}
