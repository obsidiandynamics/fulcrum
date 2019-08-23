package com.obsidiandynamics.flux;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.*;

import org.junit.*;
import org.mockito.*;

public final class PipelineCompletionHandlerTest {
  @Test
  public void testForPrintStream_withException() {
    final PrintStream stream = mock(PrintStream.class, Answers.CALLS_REAL_METHODS);
    final PipelineCompletionHandler handler = PipelineCompletionHandler.forPrintStream(stream);
    final Exception exception = new Exception("Simulated");
    handler.onComplete(exception);
    verify(stream).println(eq("Exception in pipeline"));
    verify(stream, atLeastOnce()).print((String) isNotNull());
  }
  
  @Test
  public void testForPrintStream_withoutException() {
    final PrintStream stream = mock(PrintStream.class, Answers.CALLS_REAL_METHODS);
    final PipelineCompletionHandler handler = PipelineCompletionHandler.forPrintStream(stream);
    handler.onComplete(null);
    verifyNoMoreInteractions(stream);
  }
}
