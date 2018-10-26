package com.obsidiandynamics.func.fsm;

import static com.obsidiandynamics.func.fsm.TransitionsTest.TestState.*;
import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.util.*;
import java.util.stream.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

import nl.jqno.equalsverifier.*;

public final class TransitionsTest {
  /**
   *  Test states. The labels are purely arbitrary and may mean different things depending
   *  on the test context. Not all label are used in every test.
   */
  enum TestState {
    INITIAL, INTERMEDIATE_A, INTERMEDIATE_B, INTERMEDIATE_C, INTERMEDIATE_D, FINAL
  }
  
  @Test
  public void testBuildAndValidate() {
    final Transitions<TestState> transitions = new Transitions<TestState>()
        .allow(INITIAL).to(INTERMEDIATE_A, INTERMEDIATE_B, FINAL)
        .allow(INTERMEDIATE_A).to(INTERMEDIATE_B, FINAL)
        .allow(INTERMEDIATE_B).to(INTERMEDIATE_A, FINAL);
    Assertions.assertToStringOverride(transitions);
    
    assertTrue(transitions.isAllowed(INITIAL, INITIAL));
    assertTrue(transitions.isAllowed(INITIAL, INTERMEDIATE_A));
    assertTrue(transitions.isAllowed(INITIAL, INTERMEDIATE_B));
    assertTrue(transitions.isAllowed(INITIAL, FINAL));
    assertTrue(transitions.isAllowed(INTERMEDIATE_A, INTERMEDIATE_A));
    assertTrue(transitions.isAllowed(INTERMEDIATE_A, INTERMEDIATE_B));
    assertTrue(transitions.isAllowed(INTERMEDIATE_A, FINAL));
    assertTrue(transitions.isAllowed(INTERMEDIATE_B, INTERMEDIATE_B));
    assertTrue(transitions.isAllowed(INTERMEDIATE_B, INTERMEDIATE_A));
    assertTrue(transitions.isAllowed(INTERMEDIATE_B, FINAL));
    assertTrue(transitions.isAllowed(FINAL, FINAL));

    assertTrue(transitions.isAllowed(new Transition<>(INITIAL, INTERMEDIATE_A)));

    assertFalse(transitions.isAllowed(INTERMEDIATE_A, INITIAL));
    assertFalse(transitions.isAllowed(INTERMEDIATE_B, INITIAL));
    assertFalse(transitions.isAllowed(FINAL, INTERMEDIATE_A));
    assertFalse(transitions.isAllowed(FINAL, INTERMEDIATE_B));
    assertFalse(transitions.isAllowed(FINAL, INITIAL));
    
    assertEquals(setOf(INITIAL), transitions.nonRecurring());
    
    assertTrue(transitions.isNonRecurring(INITIAL));
    assertFalse(transitions.isNonRecurring(INTERMEDIATE_A));
    assertFalse(transitions.isNonRecurring(INTERMEDIATE_B));
    assertFalse(transitions.isNonRecurring(FINAL));
    
    assertEquals(setOf(FINAL), transitions.terminal());
    
    assertTrue(transitions.isTerminal(FINAL));
    assertFalse(transitions.isTerminal(INTERMEDIATE_A));
    assertFalse(transitions.isTerminal(INTERMEDIATE_B));
    assertFalse(transitions.isTerminal(INITIAL));
  }
  
  @Test
  public void testShortest() {
    final List<TestState> p0 = asList(INTERMEDIATE_A, INTERMEDIATE_B);
    final List<TestState> p1 = asList(INTERMEDIATE_A, INTERMEDIATE_B, INTERMEDIATE_C);
    final List<TestState> p2 = asList(INTERMEDIATE_C, INTERMEDIATE_D);
    
    // rotate through the different combinations to maximise branch coverage
    assertEquals(asList(p0, p2), Transitions.shortest(asList(p0, p1, p2)));
    assertEquals(asList(p0, p2), Transitions.shortest(asList(p0, p2, p1)));
    assertEquals(asList(p0, p2), Transitions.shortest(asList(p1, p0, p2)));
    assertEquals(asList(p2, p0), Transitions.shortest(asList(p1, p2, p0)));
    assertEquals(asList(p2, p0), Transitions.shortest(asList(p2, p0, p1)));
    assertEquals(asList(p2, p0), Transitions.shortest(asList(p2, p1, p0)));
  }

  @Test
  public void testShortestPathsToSelf() {
    final Transitions<TestState> transitions = new Transitions<TestState>()
        .allow(INITIAL).to(INTERMEDIATE_A, INTERMEDIATE_B, FINAL)
        .allow(INTERMEDIATE_A).to(INTERMEDIATE_B, FINAL)
        .allow(INTERMEDIATE_B).to(INTERMEDIATE_A, FINAL);
    
    final List<List<TestState>> paths = transitions.shortestPaths(FINAL, FINAL);
    assertEquals(Collections.singletonList(Collections.emptyList()), paths);
  }

  @Test
  public void testShortestPathsNone() {
    final Transitions<TestState> transitions = new Transitions<TestState>()
        .allow(INITIAL).to(INTERMEDIATE_A, INTERMEDIATE_B, FINAL)
        .allow(INTERMEDIATE_A).to(INTERMEDIATE_B, FINAL)
        .allow(INTERMEDIATE_B).to(INTERMEDIATE_A, FINAL);
    
    final List<List<TestState>> paths = transitions.shortestPaths(FINAL, INTERMEDIATE_B);
    assertEquals(Collections.emptyList(), paths);
  }
  
  @Test
  public void testShortestPathsDirect() {
    final Transitions<TestState> transitions = new Transitions<TestState>()
        .allow(INITIAL).to(INTERMEDIATE_A, INTERMEDIATE_B, FINAL)
        .allow(INTERMEDIATE_A).to(INTERMEDIATE_B, FINAL)
        .allow(INTERMEDIATE_B).to(INTERMEDIATE_A, FINAL);
    
    {
      final List<List<TestState>> paths = transitions.shortestPaths(INITIAL, FINAL);
      assertEquals(asList(asList(FINAL)), paths);
    }
    
    {
      final List<List<TestState>> paths = transitions.shortestPaths(INTERMEDIATE_A, FINAL);
      assertEquals(asList(asList(FINAL)), paths);
    }
  }
  
  @Test
  public void testShortestPathsMultiHop() {
    final Transitions<TestState> transitions = new Transitions<TestState>()
        .allow(INITIAL).to(INTERMEDIATE_A)
        .allow(INTERMEDIATE_A).to(INTERMEDIATE_B)
        .allow(INTERMEDIATE_B).to(FINAL);
    
    {
      final List<List<TestState>> paths = transitions.shortestPaths(INITIAL, FINAL);
      assertEquals(asList(asList(INTERMEDIATE_A, INTERMEDIATE_B, FINAL)), paths);
    }
    
    {
      final List<List<TestState>> paths = transitions.shortestPaths(INTERMEDIATE_A, FINAL);
      assertEquals(asList(asList(INTERMEDIATE_B, FINAL)), paths);
    }
    
    {
      final List<List<TestState>> paths = transitions.shortestPaths(INTERMEDIATE_B, FINAL);
      assertEquals(asList(asList(FINAL)), paths);
    }
  }

  @Test
  public void testShortestPathsMultiple() {
    final Transitions<TestState> transitions = new Transitions<TestState>()
        .allow(INITIAL).to(INTERMEDIATE_A, INTERMEDIATE_B, INTERMEDIATE_C)
        .allow(INTERMEDIATE_A).to(FINAL)
        .allow(INTERMEDIATE_B).to(FINAL)
        .allow(INTERMEDIATE_C).to(INTERMEDIATE_D)
        .allow(INTERMEDIATE_D).to(FINAL);
    
    {
      final List<List<TestState>> paths = transitions.shortestPaths(INITIAL, FINAL);
      sortPathsByString(paths);
      int index = 0;
      assertEquals(asList(INTERMEDIATE_A, FINAL), paths.get(index++));
      assertEquals(asList(INTERMEDIATE_B, FINAL), paths.get(index++));
      assertEquals(index, paths.size());
    }
    
    {
      final List<List<TestState>> paths = transitions.shortestPaths(INTERMEDIATE_A, FINAL);
      sortPathsByString(paths);
      int index = 0;
      assertEquals(asList(FINAL), paths.get(index++));
      assertEquals(index, paths.size());
    }
    
    {
      final List<List<TestState>> paths = transitions.shortestPaths(INITIAL, INTERMEDIATE_A);
      sortPathsByString(paths);
      int index = 0;
      assertEquals(asList(INTERMEDIATE_A), paths.get(index++));
      assertEquals(index, paths.size());
    }
  }

  @Test
  public void testShortestPathsMultipleWithCycles() {
    final Transitions<TestState> transitions = new Transitions<TestState>()
        .allow(INITIAL).to(INTERMEDIATE_A, INTERMEDIATE_B, INTERMEDIATE_C, INTERMEDIATE_D)
        .allow(INTERMEDIATE_A).to(INTERMEDIATE_A, FINAL)
        .allow(INTERMEDIATE_B).to(INTERMEDIATE_A, INTERMEDIATE_C, FINAL)
        .allow(INTERMEDIATE_C).to(INITIAL)
        .allow(INTERMEDIATE_D).to(INTERMEDIATE_C);
    
    {
      final List<List<TestState>> paths = transitions.shortestPaths(INITIAL, FINAL);
      sortPathsByString(paths);
      int index = 0;
      assertEquals(asList(INTERMEDIATE_A, FINAL), paths.get(index++));
      assertEquals(asList(INTERMEDIATE_B, FINAL), paths.get(index++));
      assertEquals(index, paths.size());
    }
    
    {
      final List<List<TestState>> paths = transitions.shortestPaths(INTERMEDIATE_C, FINAL);
      sortPathsByString(paths);
      int index = 0;
      assertEquals(asList(INITIAL, INTERMEDIATE_A, FINAL), paths.get(index++));
      assertEquals(asList(INITIAL, INTERMEDIATE_B, FINAL), paths.get(index++));
      assertEquals(index, paths.size());
    }
    
    {
      final List<List<TestState>> paths = transitions.shortestPaths(INTERMEDIATE_D, FINAL);
      sortPathsByString(paths);
      int index = 0;
      assertEquals(asList(INTERMEDIATE_C, INITIAL, INTERMEDIATE_A, FINAL), paths.get(index++));
      assertEquals(asList(INTERMEDIATE_C, INITIAL, INTERMEDIATE_B, FINAL), paths.get(index++));
      assertEquals(index, paths.size());
    }
  }
  
  /**
   *  Sorts the given list of paths using a natural ordering of the result of calling {@link Object#toString()}
   *  of each of the state elements, so that the paths appear in a deterministic order and can be asserted.
   *  
   *  @param <S> Elemental state type.
   *  @param paths The paths to sort.
   */
  private static <S> void sortPathsByString(List<List<S>> paths) {
    sortPaths(paths, Comparator.comparing(String::valueOf));
  }
  
  private static <S> void sortPaths(List<List<S>> paths, Comparator<? super S> comparator) {
    Collections.sort(paths, (p0, p1) -> {
      assertEquals(p0.size(), p1.size());
      final int elements = p0.size();
      for (int i = 0; i < elements; i++) {
        final S e0 = p0.get(i);
        final S e1 = p1.get(i);
        final int comparison = comparator.compare(e0, e1);
        if (comparison != 0) {
          return comparison;
        }
      }
      return 0;
    });
  }
  
  @SuppressWarnings("unchecked")
  private static <S> Set<S> setOf(S... items) {
    return Arrays.stream(items).collect(Collectors.toSet());
  }
  
  @Test
  public void testGuardSuccess() throws IllegalTransitionException {
    final Transitions<TestState> transitions = new Transitions<TestState>()
        .allow(INITIAL).to(INTERMEDIATE_A, FINAL)
        .allow(INTERMEDIATE_A).to(INITIAL, FINAL);
    assertEquals(transitions.guard(INITIAL, INTERMEDIATE_A), INTERMEDIATE_A);
    assertEquals(transitions.guard(INTERMEDIATE_A, FINAL), FINAL);
    assertEquals(transitions.guard(new Transition<>(INTERMEDIATE_A, FINAL)), FINAL);
  }
  
  @Test(expected=IllegalTransitionException.class)
  public void testGuardFailure() throws IllegalTransitionException {
    final Transitions<TestState> transitions = new Transitions<TestState>()
        .allow(INITIAL).to(INTERMEDIATE_A, FINAL)
        .allow(INTERMEDIATE_A).to(INITIAL, FINAL);
    transitions.guard(FINAL, INTERMEDIATE_A);
  }
  
  @Test
  public void testEqualsHashCode() {
    EqualsVerifier.forClass(Transitions.class).verify();
  }
}
