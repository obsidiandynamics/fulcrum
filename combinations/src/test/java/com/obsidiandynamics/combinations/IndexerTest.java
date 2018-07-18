package com.obsidiandynamics.combinations;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

import nl.jqno.equalsverifier.*;

public final class IndexerTest {
  @Test
  public void testEqualsHashCode() {
    EqualsVerifier.forClass(Indexer.class).verify();
  }
  
  @Test
  public void testGetterAndToString() {
    final Indexer indexer = new Indexer(2, 3);
    assertArrayEquals(new int[] { 2, 3 }, indexer.getDimensions());
    Assertions.assertToStringOverride(indexer);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testNil() {
    new Indexer();
  }
  
  @Test
  public void testEmpty1D() {
    final Indexer indexer = new Indexer(0);
    assertEquals(0, indexer.size());
  }
  
  @Test(expected=IndexOutOfBoundsException.class)
  public void testEmpty1DOffsetTooBig() {
    final Indexer indexer = new Indexer(0);
    indexer.getIndices(0);
  }
  
  @Test
  public void testEmpty2D() {
    final Indexer indexer = new Indexer(0, 1);
    assertEquals(0, indexer.size());
  }
  
  @Test(expected=IndexOutOfBoundsException.class)
  public void testEmpty2DOffsetTooBig() {
    final Indexer indexer = new Indexer(0);
    indexer.getIndices(0);
  }
  
  @Test
  public void testSingleCell() {
    final Indexer indexer = new Indexer(1);
    assertEquals(1, indexer.size());
    assertArrayEquals(new int[] { 0 }, indexer.getIndices(0));
  }

  @Test(expected=IndexOutOfBoundsException.class)
  public void testSingleCellOffsetTooBig() {
    final Indexer indexer = new Indexer(1);
    indexer.getIndices(1);
  }
  
  @Test
  public void testVector() {
    final Indexer indexer = new Indexer(4);
    assertEquals(4, indexer.size());
    int offset = 0;
    assertArrayEquals(new int[] { 0 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 1 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 2 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 3 }, indexer.getIndices(offset++));
  }  
  
  @Test
  public void testVectorIterator() {
    final Indexer indexer = new Indexer(4);
    final Iterator<int[]> iterator = indexer.iterator();
    assertTrue(iterator.hasNext());
    assertArrayEquals(new int[] { 0 }, iterator.next());
    assertArrayEquals(new int[] { 1 }, iterator.next());
    assertArrayEquals(new int[] { 2 }, iterator.next());
    assertArrayEquals(new int[] { 3 }, iterator.next());
    assertFalse(iterator.hasNext());
  }
  
  @Test(expected=IndexOutOfBoundsException.class)
  public void testVectorOffsetTooBig() {
    final Indexer indexer = new Indexer(4);
    indexer.getIndices(4);
  }
  
  @Test
  public void testRectangle() {
    final Indexer indexer = new Indexer(2, 3);
    assertEquals(6, indexer.size());
    int offset = 0;
    assertArrayEquals(new int[] { 0, 0 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 0, 1 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 0, 2 }, indexer.getIndices(offset++));
    
    assertArrayEquals(new int[] { 1, 0 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 1, 1 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 1, 2 }, indexer.getIndices(offset++));
  }
  
  @Test
  public void testRectangleIterator() {
    final Indexer indexer = new Indexer(2, 3);
    final Iterator<int[]> iterator = indexer.iterator();
    assertTrue(iterator.hasNext());
    assertArrayEquals(new int[] { 0, 0 }, iterator.next());
    assertArrayEquals(new int[] { 0, 1 }, iterator.next());
    assertArrayEquals(new int[] { 0, 2 }, iterator.next());
    
    assertArrayEquals(new int[] { 1, 0 }, iterator.next());
    assertArrayEquals(new int[] { 1, 1 }, iterator.next());
    assertArrayEquals(new int[] { 1, 2 }, iterator.next());
    assertFalse(iterator.hasNext());
  }
  
  @Test(expected=IndexOutOfBoundsException.class)
  public void testRectangleOffsetTooBig() {
    final Indexer indexer = new Indexer(2, 3);
    indexer.getIndices(6);
  }
  
  @Test
  public void testCuboid() {
    final Indexer indexer = new Indexer(2, 3, 4);
    assertEquals(24, indexer.size());
    int offset = 0;
    assertArrayEquals(new int[] { 0, 0, 0 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 0, 0, 1 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 0, 0, 2 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 0, 0, 3 }, indexer.getIndices(offset++));
    
    assertArrayEquals(new int[] { 0, 1, 0 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 0, 1, 1 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 0, 1, 2 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 0, 1, 3 }, indexer.getIndices(offset++));
    
    assertArrayEquals(new int[] { 0, 2, 0 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 0, 2, 1 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 0, 2, 2 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 0, 2, 3 }, indexer.getIndices(offset++));

    assertArrayEquals(new int[] { 1, 0, 0 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 1, 0, 1 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 1, 0, 2 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 1, 0, 3 }, indexer.getIndices(offset++));
    
    assertArrayEquals(new int[] { 1, 1, 0 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 1, 1, 1 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 1, 1, 2 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 1, 1, 3 }, indexer.getIndices(offset++));
    
    assertArrayEquals(new int[] { 1, 2, 0 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 1, 2, 1 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 1, 2, 2 }, indexer.getIndices(offset++));
    assertArrayEquals(new int[] { 1, 2, 3 }, indexer.getIndices(offset++));
  }
  
  @Test(expected=IndexOutOfBoundsException.class)
  public void testCuboidOffsetTooBig() {
    final Indexer indexer = new Indexer(2, 3, 4);
    indexer.getIndices(24);
  }
}
