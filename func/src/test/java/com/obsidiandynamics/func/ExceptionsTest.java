package com.obsidiandynamics.func;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public class ExceptionsTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Exceptions.class);
  }
  
  private static class TestRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    TestRuntimeException(Throwable cause) { super(cause); }
  }
  
  @Test
  public void testWrapInRuntimeExceptionNormal() {
    final int out = Exceptions.wrap(() -> 42, TestRuntimeException::new);
    assertEquals(42, out);
  }
  
  @Test(expected=TestRuntimeException.class)
  public void testWrapInRuntimeExceptionThrown() {
    Exceptions.wrap(Exceptions.doThrow(IOException::new), TestRuntimeException::new);
  }
  
  @Test(expected=IOException.class)
  public void testDoThrow() throws IOException {
    Exceptions.doThrow(IOException::new).run();
  }
}
