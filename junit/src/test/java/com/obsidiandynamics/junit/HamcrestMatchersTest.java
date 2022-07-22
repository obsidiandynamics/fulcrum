package com.obsidiandynamics.junit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.math.*;
import java.util.*;
import java.util.function.*;

import org.hamcrest.*;
import org.hamcrest.core.*;
import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class HamcrestMatchersTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(HamcrestMatchers.class);
  }
  
  @Test
  public void testPredicateOf() {
    final Matcher<Object> notNullMatcher = HamcrestMatchers.fulfils(Objects::nonNull);
    assertTrue(notNullMatcher.matches("string"));
    assertFalse(notNullMatcher.matches(null));
    
    assertNotNull(notNullMatcher.toString());
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testAssertionOfCustomHandler() {
    final Consumer<AssertionError> handler = mock(Consumer.class);
    final Matcher<Object> notNullMatcher = HamcrestMatchers.assertedBy(Assert::assertNotNull, handler);
    assertTrue(notNullMatcher.matches("string"));
    verifyNoMoreInteractions(handler);
    
    assertFalse(notNullMatcher.matches(null));
    verify(handler).accept(isA(AssertionError.class));
    
    assertNotNull(notNullMatcher.toString());
  }
  
  @Test
  public void testAssertionOfDefaultHandler() {
    final Matcher<Object> notNullMatcher = HamcrestMatchers.assertedBy(Assert::assertNotNull);
    assertTrue(notNullMatcher.matches("string"));
  }
  
  @Test
  public void testInstanceOfWrongTypeWithNoNestedMatch() {
    assertThat("string", 
               IsNot.not(HamcrestMatchers.instanceOf(BigDecimal.class)
                         .thatMatches(HamcrestMatchers.fulfils(decimal -> decimal.scale() == 2))));
  }
  
  @Test
  public void testInstanceOfWrongTypeWithNestedMatch() {
    assertThat(BigDecimal.ZERO, 
               IsNot.not(HamcrestMatchers.instanceOf(Integer.class)
                         .thatMatches(HamcrestMatchers.fulfils(decimal -> decimal.toString().equals("0")))));
  }
  
  @Test
  public void testInstanceOfCorrectTypeWithNoNestedMatch() {
    assertThat(BigDecimal.ZERO, 
               IsNot.not(HamcrestMatchers.instanceOf(BigDecimal.class)
                         .thatMatches(HamcrestMatchers.fulfils(decimal -> decimal.scale() == 2))));
  }
  
  @Test
  public void testInstanceOfCorrectTypeWithNestedMatch() {
    assertThat(BigDecimal.ZERO, 
               HamcrestMatchers.instanceOf(BigDecimal.class)
               .thatMatches(HamcrestMatchers.fulfils(decimal -> decimal.scale() == 0)));
  }
  
  @Test
  public void testForPrintStream() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (PrintStream out = new PrintStream(baos)) {
      HamcrestMatchers.forPrintStream(out).accept(new AssertionError("Simulated"));
    }
    assertTrue(baos.toByteArray().length > 0);
  }
}
