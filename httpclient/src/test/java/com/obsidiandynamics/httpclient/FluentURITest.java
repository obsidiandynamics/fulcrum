package com.obsidiandynamics.httpclient;

import static org.junit.Assert.*;

import org.junit.*;

public final class FluentURITest {
  @Test
  public void testPrecanned() {
    final String uri = "https://key:master@dog.pound:90/api/path?key=value0&key=value1&standalone#fragment";
    assertEquals(uri, new FluentURI(uri).build().toString());
  }
  
  @Test
  public void testBlank() {
    assertEquals("", new FluentURI().build().toString());
  }
  
  @Test
  public void testBuildWithAll() {
    final String expected = "https://key:master@dog.pound:90/api/path?key=value0&key=value1&standalone#fragment";
    assertEquals(expected, 
                 new FluentURI()
                 .withScheme("https")
                 .withUserInfo("key", "master")
                 .withHost("dog.pound")
                 .withPort(90)
                 .withPath("/api/path")
                 .withParameter("key", "value0")
                 .withParameter("key", "value1")
                 .withParameter("standalone")
                 .withFragment("fragment")
                 .build()
                 .toString());
  }
  
  @Test
  public void testWithOptionalParameter() {
    final String expected = "https://dog.pound:90/api/path";
    assertEquals(expected,
                 new FluentURI("https://dog.pound:%d/api/path", 90)
                 .withParameter("key", null)
                 .build()
                 .toString());
  }
  
  @Test(expected=InvalidURISyntaxException.class)
  public void testWithSyntaxException() {
    new FluentURI("bad\\scheme");
  }
}
