package com.obsidiandynamics.worker;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.*;

public final class TerminatorTest {
  @Test
  public void testConstructWithArray() {
    final Terminable t0 = () -> null;
    final Terminable t1 = () -> null;
    final Terminator terminator = Terminator.of(t0, t1);
    final Collection<Terminable> view = terminator.view();
    assertEquals(2, view.size());
    assertTrue(view.contains(t0));
    assertTrue(view.contains(t1));
  }
  
  @Test
  public void testConstructWithCollection() {
    final Terminable t0 = () -> null;
    final Terminable t1 = () -> null;
    final Terminator terminator = Terminator.of(Arrays.asList(t0, t1));
    final Collection<Terminable> view = terminator.view();
    assertEquals(2, view.size());
    assertTrue(view.contains(t0));
    assertTrue(view.contains(t1));
  }
  
  @Test
  public void testTerminateAndJoin() throws InterruptedException {
    final Terminable t = mock(Terminable.class);
    final Joinable j = mock(Joinable.class);
    when(t.terminate()).thenReturn(j);
    final Terminator terminator = Terminator.of(t);

    final Joinable joinable = terminator.terminate();
    verify(t).terminate();
    assertNotNull(joinable);
    
    joinable.joinSilently();
    assertFalse(Thread.interrupted());
    verify(j).join(anyLong());
  }
  
  @Test
  public void testTerminateAndJoinBlank() throws InterruptedException {
    final Terminator terminator = Terminator.blank();

    final Joinable joinable = terminator.terminate();
    assertNotNull(joinable);
    
    final boolean joined = joinable.joinSilently(10_000);
    assertFalse(Thread.interrupted());
    assertTrue(joined);
  }
}
