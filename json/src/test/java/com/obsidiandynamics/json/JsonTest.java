package com.obsidiandynamics.json;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.junit.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

public final class JsonTest {
  static final class TestPojo {
    @JsonProperty
    int a;
    
    TestPojo withA(int a) {
      this.a = a;
      return this;
    }
    
    @Override
    public int hashCode() { return a; }
    
    @Override
    public boolean equals(Object obj) {
      return obj instanceof TestPojo && a == ((TestPojo) obj).a;
    }
  }
  
  @Test
  public void testSingleton() {
    assertSame(Json.getInstance(), Json.getInstance());
  }
  
  @Test
  public void testStringify() throws JsonOutputException {
    final TestPojo pojo = new TestPojo().withA(3);
    assertEquals("{\"a\":3}", Json.getInstance().stringify(pojo));
  }
  
  @Test
  public void testStringifyUnchecked() {
    final TestPojo pojo = new TestPojo().withA(3);
    assertEquals("{\"a\":3}", Json.getInstance().stringifyUnchecked(pojo));
  }
  
  static final class UnserializablePojo {
    int a;
  }
  
  @Test(expected=JsonOutputException.class)
  public void testStringifyError() throws JsonOutputException {
    Json.getInstance().stringify(new UnserializablePojo());
  }
  
  @Test(expected=RuntimeJsonException.class)
  public void testStringifyUncheckedError() {
    Json.getInstance().stringifyUnchecked(new UnserializablePojo());
  }
  
  @Test
  public void testParse() throws JsonInputException {
    final TestPojo parsedPojo = Json.getInstance().parse("{\"a\":3}", TestPojo.class);
    assertEquals(new TestPojo().withA(3), parsedPojo);
  }
  
  @Test
  public void testParseUnchecked() {
    final TestPojo parsedPojo = Json.getInstance().parseUnchecked("{\"a\":3}", TestPojo.class);
    assertEquals(new TestPojo().withA(3), parsedPojo);
  }
  
  @Test(expected=JsonInputException.class)
  public void testParseError() throws JsonInputException {
    Json.getInstance().parse("{\"a\":3}", Integer.class);
  }
  
  @Test(expected=RuntimeJsonException.class)
  public void testParseUncheckedError() {
    Json.getInstance().parseUnchecked("{\"a\":3}", Integer.class);
  }
  
  private static final String TEST_DATE = "2047-01-02T03:04:05.678Z";

  private static Date getTestDate() {
    final Calendar cal = Calendar.getInstance();
    cal.setTimeZone(TimeZone.getTimeZone("UTC"));
    cal.set(2047, 0, 2, 3, 4, 5);
    cal.set(Calendar.MILLISECOND, 678);
    return cal.getTime();
  }
  
  @Test
  public void testGetMapperWithIso8601() throws JsonParseException, JsonMappingException, IOException {
    final Date date = Json.getInstance().getMapper().readValue('"' + TEST_DATE + '"', Date.class);
    assertEquals(getTestDate(), date);
  }
  
  @Test
  public void testTypeOfPlain() {
    assertEquals(123L, 
                 (long) Json.getInstance().parseUnchecked("123", Json.typeOf(Long.class)));
  }
  
  @Test
  public void testTypeOfParametric() {
    assertEquals(Collections.singletonList("someString"), 
                 Json.getInstance().parseUnchecked("[\"someString\"]", Json.typeOf(List.class, String.class)));
  }
}
