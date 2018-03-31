package com.obsidiandynamics.props;

import static org.junit.Assert.*;

import java.util.*;
import java.util.function.*;
import java.util.regex.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class PropsFormatTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(PropsFormat.class);
  }
  
  @Test
  public void testLeftPad() {
    assertEquals("  x", PropsFormat.leftPad(3).apply("x"));
  }
  
  @Test
  public void testRightPad() {
    assertEquals("x  ", PropsFormat.rightPad(3).apply("x"));
  }
  
  @Test
  public void testPrefix() {
    assertEquals("_x", PropsFormat.prefix("_").apply("x"));
  }
  
  @Test
  public void testSuffix() {
    assertEquals("x_", PropsFormat.suffix("_").apply("x"));
  }
  
  @Test
  public void testStartsWith() {
    assertTrue(PropsFormat.startsWith("_").test("_x"));
    assertFalse(PropsFormat.startsWith("_").test("x"));
  }
  
  @Test
  public void testAny() {
    assertTrue(PropsFormat.any().test("foo"));
  }
  
  @Test
  public void printProps() {
    final StringBuilder logLine = new StringBuilder();
    final Properties props = new Properties();
    props.setProperty("a", "A");
    props.setProperty("b", "B");
    PropsFormat.printProps(s -> logLine.append(s).append('\n'), props, 
                              Function.identity(), Function.identity(), PropsFormat.any());
    assertEquals(2, lines(logLine));
  }
  
  @Test
  public void printPropsNoMatch() {
    final StringBuilder logLine = new StringBuilder();
    final Properties props = new Properties();
    props.setProperty("a", "A");
    props.setProperty("b", "B");
    PropsFormat.printProps(s -> logLine.append(s).append('\n'), props, 
                              Function.identity(), Function.identity(), s -> false);
    assertEquals(0, logLine.length());
  }
  
  private static int lines(StringBuilder sb) {
    final Matcher matcher = Pattern.compile("\n").matcher(sb);
    int count = 0;
    while (matcher.find()) count++;
    return count;
  }
  
  @Test
  public void printStandard() {
    final StringBuilder logLine = new StringBuilder();
    final Properties props = new Properties();
    props.setProperty("a", "A");
    props.setProperty("b", "B");
    PropsFormat.printStandard(s -> logLine.append(s).append('\n'), props, 25, "");
    assertEquals(2, lines(logLine));
  }
}
