package com.obsidiandynamics.worker;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.*;

public final class JoinerTest {
  @Test
  public void testConstructWithArray() {
    final Joinable j0 = __timeoutMillis -> true;
    final Joinable j1 = __timeoutMillis -> true;
    final Joiner joiner = Joiner.of(j0, j1);
    final Collection<Joinable> view = joiner.view();
    assertEquals(2, view.size());
    assertTrue(view.contains(j0));
    assertTrue(view.contains(j1));
  }
  
  @Test
  public void testConstructWithCollection() {
    final Joinable j0 = __timeoutMillis -> true;
    final Joinable j1 = __timeoutMillis -> true;
    final Joiner joiner = Joiner.of(Arrays.asList(j0, j1));
    final Collection<Joinable> view = joiner.view();
    assertEquals(2, view.size());
    assertTrue(view.contains(j0));
    assertTrue(view.contains(j1));
  }
  
  @Test
  public void testJoin() throws InterruptedException {
    final Joinable j = mock(Joinable.class);
    when(j.join(anyLong())).thenReturn(true);
    final Joiner joiner = Joiner.blank().add(j);
    final boolean joined = joiner.joinSilently(10_000);
    assertFalse(Thread.interrupted());
    assertTrue(joined);
    verify(j).join(anyLong());
  }
}
