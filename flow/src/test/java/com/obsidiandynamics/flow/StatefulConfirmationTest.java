package com.obsidiandynamics.flow;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class StatefulConfirmationTest {
  @Test
  public void testToString() {
    final StatefulConfirmation c = new StatefulConfirmation("id", () -> {}, () -> {});
    Assertions.assertToStringOverride(c);
  }
  
  @Test
  public void testRequestAndConfirm() {
    final FireController f = mock(FireController.class);
    final StatefulConfirmation c = new StatefulConfirmation("id", () -> {}, f);
    assertFalse(c.isConfirmed());
    assertEquals(0, c.getPendingCount());
    
    c.addRequest();
    assertFalse(c.isConfirmed());
    assertEquals(1, c.getPendingCount());
    verifyNoMoreInteractions(f);
    
    c.confirm();
    verify(f).fire();
    assertTrue(c.isConfirmed());
    assertEquals(0, c.getPendingCount());
  }
}
