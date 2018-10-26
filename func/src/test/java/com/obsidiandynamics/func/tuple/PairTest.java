package com.obsidiandynamics.func.tuple;

import static org.junit.Assert.*;

import org.junit.*;

public final class PairTest {
  @Test
  public void testGetters() {
    final Pair<String, String> pair = Pair.of("x", "y");
    assertEquals("x", pair.getFirst());
    assertEquals("y", pair.getSecond());
  }
  
  @Test
  public void testEmptySingleton() {
    assertSame(Pair.empty(), Pair.empty());
  }
  
  @Test
  public void testNonEmptyCommuteFields() {
    assertNotEquals(Pair.of("x", null), Pair.of(null, "x"));
  }
  
  @Test
  public void testCloneNonEmpty() {
    final Pair<String, String> pair = Pair.of("x", "y");
    final Pair<String, String> clone = pair.clone();
    assertEquals(pair, clone);
    assertNotSame(pair, clone);
  }
  
  @Test
  public void testCloneEmpty() {
    assertSame(Pair.empty(), Pair.empty().clone());
  }
  
  @Test
  public void testEmpty() {
    assertSame(Pair.empty(), Pair.of(null, null));
  }
  
  @Test
  public void testCompareTo() {
    assertEquals(-1, Pair.of(0, 1).compareTo(Pair.of(1, 0)));
    assertEquals(-1, Pair.empty().compareTo(Pair.of(1, 0)));
  }
}
