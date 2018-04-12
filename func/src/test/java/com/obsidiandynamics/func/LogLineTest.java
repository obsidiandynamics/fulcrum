package com.obsidiandynamics.func;

import static org.mockito.Mockito.*;

import java.io.*;

import org.junit.*;
import org.mockito.*;

public final class LogLineTest {
  @Test
  public void testNopCoverage() {
    LogLine.nop().accept("message");
  }

  @Test
  public void testPrintf() {
    final LogLine log = mock(LogLine.class, Answers.CALLS_REAL_METHODS);
    log.printf("pi is %.2f", Math.PI);
    verify(log).accept(eq("pi is 3.14"));
  }

  @Test
  public void testPrintln() {
    final LogLine log = mock(LogLine.class, Answers.CALLS_REAL_METHODS);
    log.println("message");
    verify(log).accept(eq("message"));
  }
  
  @Test
  public void testForPrintStream() {
    final PrintStream stream = mock(PrintStream.class);
    final LogLine log = LogLine.forPrintStream(stream);
    log.accept("message");
    verify(stream).println(eq("message"));
  }
}
