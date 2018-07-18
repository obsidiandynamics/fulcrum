package com.obsidiandynamics.combinations;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

public final class CombinationsTest {
  @Test(expected=IllegalArgumentException.class)
  public void testEmpty() {
    new Combinations<Character>(Collections.emptyList());
  }
  
  @Test
  public void testGetDimensions() {
    final Combinations<Character> combs = new Combinations<>(asList(asList('a', 'b', 'c'), asList('d', 'e')));
    assertArrayEquals(new int[] { 3, 2 }, combs.getDimensions());
  }
  
  @Test
  public void testGetAtLocation() {
    final Combinations<Character> combs = new Combinations<>(asList(asList('a', 'b', 'c'), asList('d', 'e')));
    assertEquals(asList('a', 'd'), combs.get(0, 0));
    assertEquals(asList('a', 'e'), combs.get(0, 1));
    assertEquals(asList('b', 'd'), combs.get(1, 0));
    assertEquals(asList('b', 'e'), combs.get(1, 1));
    assertEquals(asList('c', 'd'), combs.get(2, 0));
    assertEquals(asList('c', 'e'), combs.get(2, 1));
  }
  
  @Test
  public void testVector() {
    final Combinations<Character> combs = new Combinations<>(asList(asList('a', 'b', 'c')));
    assertEquals(3, combs.size());
    
    final List<List<Character>> all = combs.enumerate();
    assertEquals(asList(asList('a'), asList('b'), asList('c')), all);
  }
  
  @Test
  public void testRectangularEnumerate() {
    final Combinations<Character> combs = new Combinations<>(asList(asList('a', 'b', 'c'), asList('d', 'e')));
    assertEquals(6, combs.size());

    final List<List<Character>> all = combs.enumerate();
    assertEquals(asList(asList('a', 'd'), 
                        asList('a', 'e'), 
                        asList('b', 'd'), 
                        asList('b', 'e'), 
                        asList('c', 'd'), 
                        asList('c', 'e')), 
                 all);
  }
  
  @Test
  public void testRectangularIterator() {
    final Combinations<Character> combs = new Combinations<>(asList(asList('a', 'b', 'c'), asList('d', 'e')));
    final Iterator<List<Character>> iterator = combs.iterator();
    assertTrue(iterator.hasNext());
    assertEquals(asList('a', 'd'), iterator.next());
    assertEquals(asList('a', 'e'), iterator.next());
    assertEquals(asList('b', 'd'), iterator.next());
    assertEquals(asList('b', 'e'), iterator.next());
    assertEquals(asList('c', 'd'), iterator.next());
    assertEquals(asList('c', 'e'), iterator.next());
    assertFalse(iterator.hasNext());
  }
  
  @Test
  public void testRectangularWithEmptyColumn() {
    final Combinations<Character> combs = new Combinations<>(asList(asList('a', 'b', 'c'), asList()));
    assertEquals(0, combs.size());
  }
}
