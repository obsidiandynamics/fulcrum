package com.obsidiandynamics.worker;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.*;

import org.junit.*;
import org.mockito.*;

public final class WorkerExceptionHandlerTest {
  @Test
  public void testForPrintStream() {
    final PrintStream stream = mock(PrintStream.class, Answers.CALLS_REAL_METHODS);
    final WorkerExceptionHandler handler = WorkerExceptionHandler.forPrintStream(stream);
    final WorkerThread thread = WorkerThread.builder()
        .withOptions(new WorkerOptions().withName("TestThread"))
        .onCycle(__ -> {})
        .build();
    final Exception cause = new Exception("Simulated");
    handler.handle(thread, cause);
    verify(stream).println(eq("Exception in thread TestThread"));
    verify(stream, atLeastOnce()).print((String) isNotNull());
  }
  
  @Test
  public void testNop() {
    WorkerExceptionHandler.nop().handle(null, null);
  }
}
