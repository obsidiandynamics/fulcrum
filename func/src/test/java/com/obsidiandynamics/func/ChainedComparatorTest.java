package com.obsidiandynamics.func;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

public final class ChainedComparatorTest {
  @Test
  public void testEmpty() {
    final ChainedComparator<Object> comparator = new ChainedComparator<>();
    assertEquals(0, comparator.compare("one", "two"));
  }
  
  @Test
  public void testSingle() {
    final ChainedComparator<String> comparator = new ChainedComparator<String>().chain(String::compareTo);
    assertEquals(-1, comparator.compare("a", "b"));
    assertEquals(0, comparator.compare("b", "b"));
  }
  
  @Test
  public void testMultiple() {
    final ChainedComparator<String> comparator = new ChainedComparator<String>()
        .chain(compareAtIndex(0))
        .chain(compareAtIndex(1))
        .chain(compareAtIndex(2))
        .chain(compareAtIndex(3));
    
    assertEquals(-1, comparator.compare("a", "b"));
    assertEquals(0, comparator.compare("b", "b"));
    assertEquals(1, comparator.compare("b", "a"));
    
    assertEquals(0, comparator.compare("abc", "abc"));
    assertEquals(-1, comparator.compare("abc", "abd"));
    assertEquals(-1, comparator.compare("abc", "acb"));
    assertEquals(-1, comparator.compare("abc", "bba"));
  }
  
  private static Comparator<String> compareAtIndex(int index) {
    final Comparator<Character> baseComparator = Comparator.<Character>nullsFirst(Comparator.naturalOrder());
    return (str0, str1) -> {
      final Character char0 = index < str0.length() ? str0.charAt(index) : null;
      final Character char1 = index < str1.length() ? str1.charAt(index) : null;
      return baseComparator.compare(char0, char1);
    };
  }
}
