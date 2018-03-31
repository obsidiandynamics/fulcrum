package com.obsidiandynamics.props;

import static junit.framework.TestCase.*;

import java.io.*;
import java.util.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class PropsTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Props.class);
  }
  
  @Test
  public void testSystemGet() {
    assertEquals("bar", Props.get("_foo", String::valueOf, "bar"));
  }
  
  @Test
  public void testExistingValue() {
    final Properties props = new Properties();
    props.put("foo", "bar");
    assertEquals("bar", Props.get(props, "foo", String::valueOf, "baz"));
  }
  
  @Test
  public void testDefaultValue() {
    assertEquals("bar", Props.get(new Properties(), "foo", String::valueOf, "bar"));
  }
  
  @Test
  public void testGetOrSetExisting() {
    final Properties props = new Properties();
    props.put("foo", "bar");
    assertEquals("bar", Props.getOrSet(props, "foo", String::valueOf, "baz"));
    assertEquals("bar", props.getProperty("foo"));
  }
  
  @Test
  public void testGetOrSetDefault() {
    final Properties props = new Properties();
    assertEquals("baz", Props.getOrSet(props, "foo", String::valueOf, "baz"));
    assertEquals("baz", props.getProperty("foo"));
  }
  
  @Test
  public void testLoadExisting() throws IOException {
    final Properties props = Props.load("propstest.properties");
    assertTrue(props.containsKey("foo"));
  }
  
  @Test
  public void testLoadExistingDefault() throws IOException {
    final Properties props = Props.load("propstest.properties", null);
    assertNotNull(props);
    assertTrue(props.containsKey("foo"));
  }
  
  @Test(expected=FileNotFoundException.class)
  public void testLoadNonExisting() throws IOException {
    Props.load("non-existing.properties");
  }

  @Test
  public void testLoadNonExistingDefault() throws IOException {
    final Properties def = new Properties();
    def.put("foo", "bar");
    final Properties props = Props.load("non-existing.properties", def);
    assertTrue(props.containsKey("foo"));
  }
  
  @Test
  public void testMerge() {
    final Properties a = new Properties();
    a.setProperty("a", "A");
    final Properties b = new Properties();
    b.setProperty("b", "B");
    final Properties merged = Props.merge(a, b);
    assertEquals("A", merged.getProperty("a"));
    assertEquals("B", merged.getProperty("b"));
  }
}
