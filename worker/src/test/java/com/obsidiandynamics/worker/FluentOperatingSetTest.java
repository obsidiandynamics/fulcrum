package com.obsidiandynamics.worker;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class FluentOperatingSetTest {
  public final class TestSet extends FluentOperatingSet<Object, TestSet> {}
  
  @Test
  public void testToString() {
    Assertions.assertToStringOverride(new TestSet());
  }
  
  @Test
  public void testAddRemove() {
    final Terminable t0 = () -> null;
    final Terminable t1 = () -> null;
    final TestSet terminator = new TestSet().add(t0, t1);
    final Collection<Object> viewAfterAdd = terminator.view();
    assertEquals(2, viewAfterAdd.size());
    assertTrue(viewAfterAdd.contains(t0));
    assertTrue(viewAfterAdd.contains(t1));
    
    terminator.remove(t1);
    final Collection<Object> viewAfterRemove = terminator.view();
    assertEquals(1, viewAfterRemove.size());
    assertTrue(viewAfterRemove.contains(t0));
  }
  
  @Test
  public void addOptional() {
    final TestSet terminator = new TestSet().add(Optional.ofNullable(null));
    assertEquals(0, terminator.view().size());
  }
}
