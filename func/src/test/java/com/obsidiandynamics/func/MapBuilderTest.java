package com.obsidiandynamics.func;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class MapBuilderTest {
  @Test
  public void testEmptyWithDefaultMapImpl() {
    assertEquals(Collections.emptyMap(), new MapBuilder<>().build());
  }
  
  @Test
  public void testPutWithCustomMapImpl() {
    final Map<Object, Object> expected = new TreeMap<>();
    expected.put("key0", "value0");
    expected.put("key1", "value1");
    
    assertEquals(expected, 
                 new MapBuilder<>(new TreeMap<>())
                 .with("key0", "value0").with("key1", "value1").build());
  }
  
  @Test
  public void testInit() {
    assertEquals(new MapBuilder<Integer, Long>().with(10, 100L).build(),
                 MapBuilder.init(10,  100L).build());
  }
  
  @Test
  public void testToString() {
    Assertions.assertToStringOverride(new MapBuilder<>());
  }
}
