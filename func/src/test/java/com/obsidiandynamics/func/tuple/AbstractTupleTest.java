package com.obsidiandynamics.func.tuple;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.util.*;
import java.util.function.*;

import org.junit.*;
import org.junit.rules.*;

import nl.jqno.equalsverifier.*;

public final class AbstractTupleTest {
  private static final class TestPair<X, Y> extends AbstractPair<X, Y> {
    TestPair(X first, Y second) {
      super(first, second);
    }
  }
  
  private static <X, Y> TestPair<X, Y> pair(X first, Y second) {
    return new TestPair<>(first, second);
  }
  
  @Rule
  public final ExpectedException expectedException = ExpectedException.none();
  
  @Test
  public void testDefaultToString() {
    assertEquals("TestPair [x, y]", pair("x", "y").toString());
  }
  
  @Test
  public void testEqualsHashCode() {
    EqualsVerifier.forClass(TestPair.class).withRedefinedSuperclass().verify();
  }
  
  @Test
  public void testCustomToString() {
    class CustomPair extends AbstractPair<String, String> {
      CustomPair(String first, String second) {
        super(first, second);
      }
      
      @Override
      protected Function<Object, String> getFormatter(int fieldIndex) {
        return fieldIndex == 0 ? obj -> ((String) obj).toUpperCase() : super.getFormatter(fieldIndex);
      }
    };
    
    final CustomPair pair = new CustomPair("a", "b");
    assertEquals("CustomPair [A, b]", pair.toString());
  }
  
  @Test
  public void testDefaultComparatorImplicit() {
    final List<TestPair<Integer, Integer>> pairs = Arrays.asList(pair(2, 2), pair(0, 0), pair(1, 2), pair(1, 2), pair(1, 0));
    Collections.sort(pairs);
    assertEquals(asList(pair(0, 0), pair(1, 0), pair(1, 2), pair(1, 2), pair(2, 2)), pairs);
  }
  
  @Test
  public void testDefaultComparatorReversed() {
    final List<TestPair<Integer, Integer>> pairs = Arrays.asList(pair(2, 2), pair(0, 0), pair(1, 2), pair(1, 2), pair(1, 0));
    Collections.sort(pairs, AbstractTuple.defaultComparator().reversed());
    assertEquals(asList(pair(2, 2), pair(1, 2), pair(1, 2), pair(1, 0), pair(0, 0)), pairs);
  }
  
  @Test
  public void testDefaultComparatorWithNulls() {
    final List<TestPair<Integer, Integer>> pairs = Arrays.<TestPair<Integer, Integer>>asList(pair(2, 2), pair(null, 0), pair(1, null), pair(1, 2));
    Collections.sort(pairs);
    assertEquals(asList(pair(null, 0), pair(1, null), pair(1, 2), pair(2, 2)), pairs);
  }
  
  @Test
  public void testCompareHeterogenousTuples() {
    class PairImpl1<X, Y> extends AbstractPair<X, Y> {
      PairImpl1(X first, Y second) {
        super(first, second);
      }
    }
    
    class PairImpl2<X, Y> extends AbstractPair<X, Y> {
      PairImpl2(X first, Y second) {
        super(first, second);
      }
    }
    
    final List<AbstractPair<Integer, Integer>> pairs = Arrays.asList(new PairImpl1<>(0, 0), new PairImpl2<>(0, 0));
    
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Classes are not mutually comparable: ");
    expectedException.expectMessage(PairImpl1.class.getName());
    expectedException.expectMessage(PairImpl2.class.getName());
    expectedException.expectMessage(" and ");
    Collections.sort(pairs, AbstractPair.defaultComparator());
  }
  
  @Test
  public void testEqualsForDifferentImplementations() {
    class PairImpl1<X, Y> extends AbstractPair<X, Y> {
      PairImpl1(X first, Y second) {
        super(first, second);
      }
    }
    
    class PairImpl2<X, Y> extends AbstractPair<X, Y> {
      PairImpl2(X first, Y second) {
        
        super(first, second);
      }
    }
    assertNotEquals(new PairImpl1<>("a", "b"), new PairImpl2<>("a", "b"));
  }
}
