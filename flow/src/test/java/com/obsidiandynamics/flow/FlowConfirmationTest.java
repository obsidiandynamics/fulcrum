package com.obsidiandynamics.flow;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class FlowConfirmationTest {
  @Test
  public void testToString() {
    final FlowConfirmation c = new FlowConfirmation("id", () -> {});
    Assertions.assertToStringOverride(c);
  }
  
  @Test
  public void testRequestAndConfirm() {
    final FlowConfirmation c = new FlowConfirmation("id", () -> {});
    assertFalse(c.isConfirmed());
    assertEquals(0, c.getPendingCount());
    
    c.addRequest();
    assertFalse(c.isConfirmed());
    assertEquals(1, c.getPendingCount());
    
    c.confirm();
    assertTrue(c.isConfirmed());
    assertEquals(0, c.getPendingCount());
  }
}
