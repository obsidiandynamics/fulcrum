package com.obsidiandynamics.httpclient;

import static org.junit.Assert.*;

import org.assertj.core.api.*;
import org.junit.*;

public final class ResponseStatusExceptionTest {
  @Test
  public void testAbbreviate() {
    assertEquals("", ResponseStatusException.abbreviate("", 3));
    assertEquals("123", ResponseStatusException.abbreviate("123", 5));
    assertEquals("1234", ResponseStatusException.abbreviate("1234", 5));
    assertEquals("12345", ResponseStatusException.abbreviate("12345", 5));
    assertEquals("12...", ResponseStatusException.abbreviate("123456", 5));
    
    Assertions.assertThatThrownBy(() -> {
      ResponseStatusException.abbreviate("foobar", 2);
    })
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessage("Length must not be shorter than 3");
  }
}
