package com.obsidiandynamics.func;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class ArrayCopyTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(ArrayCopy.class);
  }

  @Test
  public void testAllocate() {
    final int[] a = ArrayCopy.allocate(int[].class, 3);
    assertNotNull(a);
    assertEquals(int[].class, a.getClass());
    assertEquals(3, a.length);
  }

  @Test(expected=IllegalArgumentException.class)
  public void testAllocate_nullTypeError() {
    ArrayCopy.allocate(null, 0);
  }

  @Test(expected=IllegalArgumentException.class)
  public void testAllocateNonArrayTypeError() {
    ArrayCopy.allocate(int.class, 0);
  }

  @Test
  public void testGrow_intsFromTail() {
    final int[] ints = { 4, 5, 6, 7 };
    final int[] newInts = ArrayCopy.grow(ints, 2, 0);
    assertNotNull(newInts);
    assertNotSame(ints, newInts);
    assertEquals(int[].class, newInts.getClass());
    assertArrayEquals(new int[] { 4, 5, 6, 7, 0, 0 }, newInts);
  }

  @Test
  public void testGrow_intsFromHead() {
    final int[] ints = { 4, 5, 6, 7 };
    final int[] newInts = ArrayCopy.grow(ints, 2, 2);
    assertArrayEquals(new int[] { 0, 0, 4, 5, 6, 7 }, newInts);
  }

  @Test
  public void testGrow_intsFromBothEnds() {
    final int[] ints = { 4, 5, 6, 7 };
    final int[] newInts = ArrayCopy.grow(ints, 2, 1);
    assertArrayEquals(new int[] { 0, 4, 5, 6, 7, 0 }, newInts);
  }
  
  @Test
  public void testGrow_intsNone() {
    final int[] ints = { 4, 5, 6, 7 };
    final int[] newInts = ArrayCopy.grow(ints, 0, 0);
    assertNotNull(newInts);
    assertNotSame(ints, newInts);
    assertEquals(int[].class, newInts.getClass());
    assertArrayEquals(new int[] { 4, 5, 6, 7 }, newInts);
  }

  @Test
  public void testGrow_strings() {
    final String[] strings = { "a", "b", "c" };
    final String[] newStrings = ArrayCopy.grow(strings, 2, 0);
    assertNotNull(newStrings);
    assertNotSame(strings, newStrings);
    assertEquals(String[].class, newStrings.getClass());
    assertArrayEquals(new String[] { "a", "b", "c", null, null }, newStrings);
  }

  @Test
  public void testGrow_arrays() {
    final int[] i01 = { 0, 1 };
    final int[] i23 = { 2, 3 };
    final int[][] pairs = { i01, i23 };
    final int[][] newPairs = ArrayCopy.grow(pairs, 1, 0);
    assertNotNull(newPairs);
    assertNotSame(pairs, newPairs);
    assertEquals(int[][].class, newPairs.getClass());
    assertArrayEquals(new int[][] { i01, i23, null }, newPairs);
  }
  
  @Test
  public void testGrow_generics() {
    final Optional<?>[] generics = { Optional.of(0), Optional.of(1) };
    final Optional<?>[] newGenerics = ArrayCopy.grow(generics, 1, 0);
    assertNotNull(newGenerics);
    assertNotSame(generics, newGenerics);
    assertEquals(Optional[].class, newGenerics.getClass());
    assertArrayEquals(new Optional[] { Optional.of(0), Optional.of(1), null }, newGenerics);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testGrow_addNegativeError() {
    ArrayCopy.grow(new int[] { 4, 5, 6, 7 }, -1, 0);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testGrow_shiftNegativeError() {
    ArrayCopy.grow(new int[] { 4, 5, 6, 7 }, 0, -1);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testGrow_shiftMoreThanAddError() {
    ArrayCopy.grow(new int[] { 4, 5, 6, 7 }, 1, 2);
  }
  
  @Test
  public void testAppendSingle_ints() {
    final int[] ints = { 4, 5, 6, 7 };
    final int[] newInts = ArrayCopy.append(ints, 8);
    assertArrayEquals(new int[] { 4, 5, 6, 7, 8 }, newInts);
  }
  
  @Test
  public void testAppendVargs_ints() {
    final int[] ints = { 4, 5, 6, 7 };
    final int[] newInts = ArrayCopy.append(ints, 8, 9);
    assertArrayEquals(new int[] { 4, 5, 6, 7, 8, 9 }, newInts);
  }
  
  @Test
  public void testSlice_intsKeepingMiddle() {
    final int[] ints = { 4, 5, 6, 7 };
    final int[] newInts = ArrayCopy.slice(ints, 1, 3);
    assertArrayEquals(new int[] { 5, 6 }, newInts);
  }
  
  @Test
  public void testSlice_intsKeepingHead() {
    final int[] ints = { 4, 5, 6, 7 };
    final int[] newInts = ArrayCopy.slice(ints, 0, 2);
    assertArrayEquals(new int[] { 4, 5 }, newInts);
  }
  
  @Test
  public void testSlice_intsKeepingTail() {
    final int[] ints = { 4, 5, 6, 7 };
    final int[] newInts = ArrayCopy.slice(ints, 2, 4);
    assertArrayEquals(new int[] { 6, 7 }, newInts);
  }
  
  @Test
  public void testSlice_empty() {
    final int[] ints = { 4, 5, 6, 7 };
    final int[] newInts = ArrayCopy.slice(ints, 2, 2);
    assertArrayEquals(new int[0], newInts);
  }
  
  @Test(expected=ArrayIndexOutOfBoundsException.class)
  public void testSlice_fromNegativeError() {
    ArrayCopy.slice(new int[] { 4, 5, 6, 7 }, -1, 0);
  }
  
  @Test(expected=ArrayIndexOutOfBoundsException.class)
  public void testSlice_toOverflowError() {
    ArrayCopy.slice(new int[] { 4, 5, 6, 7 }, 0, 5);
  }
  
  @Test(expected=ArrayIndexOutOfBoundsException.class)
  public void testSlice_fromToCrossError() {
    ArrayCopy.slice(new int[] { 4, 5, 6, 7 }, 1, 0);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testSlice_nullError() {
    ArrayCopy.slice(null, 0, 0);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testSlice_nonArrayError() {
    ArrayCopy.slice(42, 0, 0);
  }
}
