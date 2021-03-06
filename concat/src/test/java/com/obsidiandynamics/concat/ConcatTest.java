package com.obsidiandynamics.concat;

import static org.junit.Assert.*;

import org.junit.*;

public final class ConcatTest {
  @Test
  public void testBooleanConditionPass() {
    final CharSequence cs = new Concat("foo")
        .when(true).append(" bar");
    assertEquals("foo bar", cs.toString());
  }
  
  @Test
  public void testBooleanConditionFail() {
    final CharSequence cs = new Concat()
        .append("foo")
        .when(false).append(" bar");
    assertEquals("foo", cs.toString());
  }
  
  @Test
  public void testIsNullConditionPass() {
    final CharSequence cs = new Concat("foo")
        .whenIsNull(null).append(" bar");
    assertEquals("foo bar", cs.toString());
  }
  
  @Test
  public void testIsNullConditionFail() {
    final CharSequence cs = new Concat()
        .append("foo")
        .whenIsNull(this).append(" bar");
    assertEquals("foo", cs.toString());
  }
  
  @Test
  public void testIsNotNullConditionPass() {
    final CharSequence cs = new Concat("foo")
        .whenIsNotNull(this).append(" bar");
    assertEquals("foo bar", cs.toString());
  }
  
  @Test
  public void testIsNotNullConditionFail() {
    final CharSequence cs = new Concat()
        .append("foo")
        .whenIsNotNull(null).append(" bar");
    assertEquals("foo", cs.toString());
  }
  
  @Test
  public void testAppendArray() {
    assertEquals("foo bar", new Concat().when(true).appendArray(" ", "foo", "bar").toString());
  }
  
  @Test
  public void testLength() {
    assertEquals(5, new Concat("hello").length());
  }
  
  @Test
  public void testCharAt() {
    assertEquals('e', new Concat("hello").charAt(1));
  }
  
  @Test
  public void testSubSequence() {
    assertEquals("ell", new Concat("hello").subSequence(1, 4));
  }
}
