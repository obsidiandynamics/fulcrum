package com.obsidiandynamics.func;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.*;

import org.junit.*;

public final class ExceptionHandlerTest {
  @Test
  public void testNop() {
    // coverage testing
    ExceptionHandler.nop().onException("summary", null);
  }
  
  @Test
  public void testPrintStreamSummaryOnly() {
    final PrintStream stream = mock(PrintStream.class);
    ExceptionHandler.forPrintStream(stream).onException("summary", null);
    verify(stream).println(any(String.class));
  }
  
  @Test
  public void testPrintStreamCauseOnly() {
    final PrintStream stream = mock(PrintStream.class);
    final Exception cause = new Exception("test exception");
    ExceptionHandler.forPrintStream(stream).onException(null, cause);
    verify(stream, atLeastOnce()).println(any(Object.class));
  }
}
