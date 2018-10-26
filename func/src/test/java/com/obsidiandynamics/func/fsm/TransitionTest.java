package com.obsidiandynamics.func.fsm;

import static org.junit.Assert.*;

import org.junit.*;

import nl.jqno.equalsverifier.*;

public final class TransitionTest {
  @Test
  public void testEqualsHashCode() {
    EqualsVerifier.forClass(Transition.class).verify();
  }
  
  @Test
  public void testPojo() {
    final Transition<String> transition = new Transition<>("fromState", "toState");
    assertEquals("fromState", transition.getFrom());
    assertEquals("toState", transition.getTo());
    assertEquals("fromState -> toState", transition.toString());
  }
  
  @Test
  public void testToString() {
    final Transition<String> transition = new Transition<>("fromState", "toState");
    assertTrue(transition.toString().contains(transition.getFrom()));
    assertTrue(transition.toString().contains(transition.getTo()));
  }
}
