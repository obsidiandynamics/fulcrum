package com.obsidiandynamics.func.tuple;

import static org.junit.Assert.*;

import org.junit.*;

public final class QuadrupleTest {
  @Test
  public void testGetters() {
    final Quadruple<String, String, String, String> pair = Quadruple.of("w", "x", "y", "z");
    assertEquals("w", pair.getFirst());
    assertEquals("x", pair.getSecond());
    assertEquals("y", pair.getThird());
    assertEquals("z", pair.getFourth());
  }
  
  @Test
  public void testEmptySingleton() {
    assertSame(Quadruple.empty(), Quadruple.empty());
  }
  
  @Test
  public void testNonEmptyCommuteFields() {
    assertNotEquals(Quadruple.of("x", null, null, null), Quadruple.of(null, "x", null, null));
    assertNotEquals(Quadruple.of(null, "x", null, null), Quadruple.of(null, null, "x", null));
    assertNotEquals(Quadruple.of(null, null, "x", null), Quadruple.of(null, null, null, "x"));
  }
  
  @Test
  public void testCloneNonEmpty() {
    final Quadruple<String, String, String, String> pair = Quadruple.of("w", "x", "y", "z");
    final Quadruple<String, String, String, String> clone = pair.clone();
    assertEquals(pair, clone);
    assertNotSame(pair, clone);
  }
  
  @Test
  public void testCloneEmpty() {
    assertSame(Quadruple.empty(), Quadruple.empty().clone());
  }
  
  @Test
  public void testEmpty() {
    assertSame(Quadruple.empty(), Quadruple.of(null, null, null, null));
  }
  
  @Test
  public void testCompareTo() {
    assertEquals(-1, Quadruple.of(0, 1, 2, 3).compareTo(Quadruple.of(1, 0, 2, 3)));
    assertEquals(-1, Quadruple.empty().compareTo(Quadruple.of(1, 0, 2, 3)));
  }
}
