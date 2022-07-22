package com.obsidiandynamics.json;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.junit.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.obsidiandynamics.func.*;

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
  public void testFormat() throws JsonOutputException {
    final TestPojo pojo = new TestPojo().withA(3);
    assertEquals("{\"a\":3}", Json.getInstance().format(pojo));
  }

  @Test
  public void testFormatUnchecked() {
    final TestPojo pojo = new TestPojo().withA(3);
    assertEquals("{\"a\":3}", Json.getInstance().formatUnchecked(pojo));
  }

  static final class UnserializablePojo {
    int a;
  }

  @Test(expected=JsonOutputException.class)
  public void testFormatError() throws JsonOutputException {
    Json.getInstance().format(new UnserializablePojo());
  }

  @Test(expected=RuntimeJsonException.class)
  public void testFormatUncheckedError() {
    Json.getInstance().formatUnchecked(new UnserializablePojo());
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

  @Test
  public void testParser() throws JsonInputException {
    final CheckedFunction<String, TestPojo, JsonInputException> parser = Json.getInstance().parser(TestPojo.class);
    assertEquals(new TestPojo().withA(3), parser.apply("{\"a\":3}"));
  }

  @Test
  public void testParserJavaType() throws JsonInputException {
    final CheckedFunction<String, TestPojo, JsonInputException> parser = Json.getInstance().parser(Json.typeOf(TestPojo.class));
    assertEquals(new TestPojo().withA(3), parser.apply("{\"a\":3}"));
  }

  @Test
  public void testUncheckedParser() throws JsonInputException {
    final Function<String, TestPojo> parser = Json.getInstance().uncheckedParser(TestPojo.class);
    assertEquals(new TestPojo().withA(3), parser.apply("{\"a\":3}"));
  }

  @Test
  public void testUncheckedParserJavaType() throws JsonInputException {
    final Function<String, Object> parser = Json.getInstance().uncheckedParser(Json.typeOf(TestPojo.class));
    assertEquals(new TestPojo().withA(3), parser.apply("{\"a\":3}"));
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
  public void testGetMapperWithIso8601() throws IOException {
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
