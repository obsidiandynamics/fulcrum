package com.obsidiandynamics.flow;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.*;

import pl.pojo.tester.internal.assertion.tostring.*;

public final class StatefulConfirmationTest {
  @Test
  public void testToString() {
    final Runnable task = () -> {};
    final StatefulConfirmation c = new StatefulConfirmation("testId", task, () -> {});
    c.addRequest();
    c.addRequest();
    c.confirm();
    final ToStringAssertions toStringAssertions = new ToStringAssertions(c);
    toStringAssertions.contains("id", "testId");
    toStringAssertions.contains("requested", "2");
    toStringAssertions.contains("completed", "1");
    toStringAssertions.contains("onComplete", task);
  }
  
  @Test
  public void testRequestAndConfirm() {
    final FireController f = mock(FireController.class);
    final StatefulConfirmation c = new StatefulConfirmation("testId", () -> {}, f);
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
  
  @Test
  public void testConfirm_illegalStateTooManyCompletions() {
    final FireController f = mock(FireController.class);
    final StatefulConfirmation c = new StatefulConfirmation("testId", () -> {}, f);
    
    c.addRequest();
    c.confirm();
    verify(f).fire();
    reset(f);
    assertTrue(c.isConfirmed());
    
    assertThatThrownBy(c::confirm).isExactlyInstanceOf(IllegalStateException.class).hasMessage("Completed 2 of 1 for ID testId");
    assertEquals(0, c.getPendingCount());
    assertTrue(c.isConfirmed());
    verifyNoMoreInteractions(f);
  }
}
