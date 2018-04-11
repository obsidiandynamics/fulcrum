package com.obsidiandynamics.func;

import static org.mockito.Mockito.*;

import java.io.*;

import org.junit.*;
import org.mockito.*;

public final class LogLineTest {
  @Test
  public void testNopCoverage() {
    LogLine.nop().println("message");
  }

  @Test
  public void testFormat() {
    final LogLine log = mock(LogLine.class, Answers.CALLS_REAL_METHODS);
    log.printf("pi is %.2f", 3.14);
    verify(log).println(eq("pi is 3.14"));
  }
  
  @Test
  public void testForPrintStream() {
    final PrintStream stream = mock(PrintStream.class);
    final LogLine log = LogLine.forPrintStream(stream);
    log.println("message");
    verify(stream).println(eq("message"));
  }
}
