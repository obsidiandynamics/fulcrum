package com.obsidiandynamics.threads;

import static com.obsidiandynamics.func.Functions.*;
import static org.junit.Assert.*;

import java.util.*;

import org.assertj.core.api.*;
import org.junit.*;

import com.obsidiandynamics.func.tuple.*;

import pl.pojo.tester.internal.assertion.tostring.*;

public final class ReferenceCountingMapTest {
  @Test
  public void testMethods_size_isEmpty_keySet_containsKey_toString() {
    final ReferenceCountingMap<String, String> map = new ReferenceCountingMap<>();
    
    assertTrue(map.isEmpty());
    assertEquals(0, map.size());
    assertFalse(map.containsKey("a"));
    Assertions.assertThat(map.keySet()).isEmpty();
    
    map.tryScope("a");
    assertTrue(map.isEmpty());
    assertEquals(0, map.size());
    assertFalse(map.containsKey("a"));
    Assertions.assertThat(map.keySet()).isEmpty();
    
    map.scope("a", givePlain("alfa"));
    map.scope("b", givePlain("bravo"));
    assertFalse(map.isEmpty());
    assertEquals(2, map.size());
    assertTrue(map.containsKey("a"));
    Assertions.assertThat(map.keySet()).containsExactly("a", "b");
    new ToStringAssertions(map).contains("keySet", map.keySet());
    
    map.descope("a");
    map.descope("b");
    assertTrue(map.isEmpty());
    assertEquals(0, map.size());
    assertFalse(map.containsKey("a"));
    Assertions.assertThat(map.keySet()).isEmpty();
  }
  
  @Test
  public void testScopeDescope() {
    final ReferenceCountingMap<String, String> map = new ReferenceCountingMap<>();
    
    assertEquals("alfa", map.scope("a", givePlain("alfa")));
    assertTrue(map.containsKey("a"));
    
    assertEquals("alfa", map.scope("a", givePlain("alfa")));
    assertTrue(map.containsKey("a"));
    
    map.descope("a");
    assertTrue(map.containsKey("a"));
    
    map.descope("a");
    assertEquals(0, map.size());
  }
  
  @Test
  public void testTryScope() {
    final ReferenceCountingMap<String, String> map = new ReferenceCountingMap<>();
    
    assertNull(map.tryScope("a"));

    assertEquals("alfa", map.scope("a", givePlain("alfa")));
    assertTrue(map.containsKey("a"));
    
    assertEquals("alfa", map.tryScope("a"));
    
    map.descope("a");
    assertTrue(map.containsKey("a"));
    
    map.descope("a");
    assertEquals(0, map.size());
  }
  
  @Test
  public void testForEach() {
    final ReferenceCountingMap<String, String> map = new ReferenceCountingMap<>();
    map.scope("a", givePlain("alfa"));
    map.scope("b", givePlain("bravo"));
    map.scope("c", givePlain("charlie"));
    
    final List<Pair<String, String>> entries = new ArrayList<>(2);
    map.forEach((k, v) -> {
      // disrupt the contents of the map while iterating through the entries
      if (k.equals("a")) {
        map.descope("b");
      }
      
      entries.add(Pair.of(k, v));
    });
    
    Assertions.assertThat(entries).containsExactly(Pair.of("a", "alfa"), Pair.of("c", "charlie"));
    assertEquals(2, map.size());
    
    map.descope("a");
    map.descope("c");
    assertEquals(0, map.size());
  }
}
