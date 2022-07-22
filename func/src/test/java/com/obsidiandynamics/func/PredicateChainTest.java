package com.obsidiandynamics.func;

import static org.junit.Assert.*;

import java.util.function.*;

import org.junit.*;

public final class PredicateChainTest {
  @Test
  public void testEmptyAll() {
    final Predicate<Integer> predicate = new PredicateChain<Integer>().allMatch();
    assertTrue(predicate.test(0));
  }

  @Test
  public void testChainedAll() {
    final Predicate<Integer> predicate = PredicateChain.allOf(i -> i > 3, i -> i < 6);
    
    assertFalse(predicate.test(3));
    assertTrue(predicate.test(4));
    assertTrue(predicate.test(5));
    assertFalse(predicate.test(6));
  }

  @Test
  public void testEmptyAny() {
    final Predicate<Integer> predicate = new PredicateChain<Integer>().anyMatch();
    assertFalse(predicate.test(0));
  }

  @Test
  public void testChainedAny() {
    final Predicate<Integer> predicate = PredicateChain.anyOf(i -> i < 4, i -> i > 5);
    
    assertTrue(predicate.test(3));
    assertFalse(predicate.test(4));
    assertFalse(predicate.test(5));
    assertTrue(predicate.test(6));
  }
}
