package com.obsidiandynamics.threads;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

import org.junit.*;

import com.obsidiandynamics.await.*;

public class ParallelTest {
  @Test
  public void testBlocking() {
    final Set<Integer> ran = new CopyOnWriteArraySet<>();
    final int threads = 4;
    
    Parallel.blocking(threads, ran::add).run();
    assertEquals(threads, ran.size());
  }
  
  @Test
  public void testNonBlocking() {
    final Set<Integer> ran = new CopyOnWriteArraySet<>();
    final int threads = 4;
    
    Parallel.nonBlocking(threads, ran::add).run();
    Timesert.wait(10_000).until(() -> {
      assertEquals(threads, ran.size());
    });
  }
  
  @Test
  public void testSplit() {
    final List<Integer> items = asList(0, 1, 2, 3);
    assertEquals(asList(asList(0, 1, 2, 3)), Parallel.split(items, 1));
    assertEquals(asList(asList(0, 1), asList(2, 3)), Parallel.split(items, 2));
    assertEquals(asList(asList(0), asList(1), asList(2, 3)), Parallel.split(items, 3));
    assertEquals(asList(asList(0), asList(1), asList(2), asList(3)), Parallel.split(items, 4));
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testSplitIllegalArgumentTooLow() {
    Parallel.split(asList(0, 1), 0);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testSplitIllegalArgumentTooHigh() {
    Parallel.split(asList(0, 1), 3);
  }
  
  @Test
  public void testBlockingSlice() {
    final int numItems = 16;
    final List<Integer> items = IntStream.range(0, numItems).boxed().collect(Collectors.toList());
    final Set<List<Integer>> ran = new CopyOnWriteArraySet<>();
    final int threads = 4;
    
    Parallel.blockingSlice(items, threads, ran::add).run();
    assertEquals(threads, ran.size());
    final List<List<Integer>> split = Parallel.split(items, 4);
    for (List<Integer> s : split) {
      assertTrue("ran=" + ran, ran.contains(s));
    }
  }
  
  @Test
  public void testNonBlockingSlice() {
    final int numItems = 16;
    final List<Integer> items = IntStream.range(0, numItems).boxed().collect(Collectors.toList());
    final Set<List<Integer>> ran = new CopyOnWriteArraySet<>();
    final int threads = 4;
    
    Parallel.nonBlockingSlice(items, threads, ran::add).run();
    Timesert.wait(10_000).until(() -> {
      assertEquals(threads, ran.size());
    });
    final List<List<Integer>> split = Parallel.split(items, 4);
    for (List<Integer> s : split) {
      assertTrue("ran=" + ran, ran.contains(s));
    }
  }
}
