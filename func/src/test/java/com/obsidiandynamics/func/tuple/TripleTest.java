package com.obsidiandynamics.func.tuple;

import static org.junit.Assert.*;

import org.junit.*;

public final class TripleTest {
  @Test
  public void testGetters() {
    final Triple<String, String, String> pair = Triple.of("x", "y", "z");
    assertEquals("x", pair.getFirst());
    assertEquals("y", pair.getSecond());
    assertEquals("z", pair.getThird());
  }
  
  @Test
  public void testEmptySingleton() {
    assertSame(Triple.empty(), Triple.empty());
  }
  
  @Test
  public void testNonEmptyCommuteFields() {
    assertNotEquals(Triple.of("x", null, null), Triple.of(null, "x", null));
    assertNotEquals(Triple.of(null, "x", null), Triple.of(null, null, "x"));
  }
  
  @Test
  public void testCloneNonEmpty() {
    final Triple<String, String, String> pair = Triple.of("x", "y", "z");
    final Triple<String, String, String> clone = pair.clone();
    assertEquals(pair, clone);
    assertNotSame(pair, clone);
  }
  
  @Test
  public void testCloneEmpty() {
    assertSame(Triple.empty(), Triple.empty().clone());
  }
  
  @Test
  public void testEmpty() {
    assertSame(Triple.empty(), Triple.of(null, null, null));
  }
  
  @Test
  public void testCompareTo() {
    assertEquals(-1, Triple.of(0, 1, 2).compareTo(Triple.of(1, 0, 2)));
    assertEquals(-1, Triple.empty().compareTo(Triple.of(1, 0, 2)));
  }
}
