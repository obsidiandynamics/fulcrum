package com.obsidiandynamics.testsupport;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;
import com.obsidiandynamics.func.*;
import com.obsidiandynamics.testsupport.Testmark.*;

public final class TestmarkTest {
  @After
  public void after() {
    Testmark.reset();
  }
  
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Testmark.class);
  }
  
  @Test
  public void testNotEnabled() {
    final CheckedRunnable<?> r = mock(CheckedRunnable.class);
    assertFalse(Testmark.isEnabled());
    Testmark.ifEnabled(r);
    verifyNoMoreInteractions(r);
  }
  
  @Test
  public void testEnabledNoName() throws Exception {
    final CheckedRunnable<?> r = mock(CheckedRunnable.class);
    final LogLine logLine = mock(LogLine.class);
    Testmark.enable().withOptions(LogLine.class, logLine);
    assertTrue(Testmark.isEnabled());
    
    Testmark.ifEnabled(r);
    verify(r).run();
    verify(logLine).accept(isNotNull());
  }
  
  @Test
  public void testEnabledWithName() throws Exception {
    final CheckedRunnable<?> r = mock(CheckedRunnable.class);
    final LogLine logLine = mock(LogLine.class);
    Testmark.enable().withOptions(LogLine.class, logLine);
    assertTrue(Testmark.isEnabled());
    
    Testmark.ifEnabled("name", r);
    verify(r).run();
    verify(logLine).accept(isNotNull());
  }
  
  @Test
  public void testSysOut() {
    assertNotNull(Testmark.sysOut());
  }
  
  @Test
  public void testPrintStreamExceptionHandler() {
    final PrintStream stream = mock(PrintStream.class);
    final ExceptionHandler printStreamExceptionHandler = Testmark.printStreamExceptionHandler(stream);
    printStreamExceptionHandler.accept(new Exception("test exception"));
    verify(stream, atLeastOnce()).println(isA(Object.class));
  }
  
  @Test
  public void testSysErrExceptionHandler() {
    assertNotNull(Testmark.sysErrExceptionHandler());
  }
  
  @Test
  public void testEnabledWithError() throws Exception {
    final CheckedRunnable<?> r = mock(CheckedRunnable.class);
    final Exception cause = new Exception("test exception");
    doThrow(cause).when(r).run();
    final LogLine logLine = mock(LogLine.class);
    final ExceptionHandler exceptionHandler = mock(ExceptionHandler.class);
    Testmark.enable().withOptions(LogLine.class, logLine).withOptions(ExceptionHandler.class, exceptionHandler);
    assertTrue(Testmark.isEnabled());
    
    Testmark.ifEnabled(r);
    verify(r).run();
    verify(logLine).accept(isNotNull());
    verify(exceptionHandler).accept(eq(cause));
  }
  
  @Test
  public void testEnabledWithScale() throws Exception {
    final CheckedRunnable<?> r = mock(CheckedRunnable.class);
    final LogLine logLine = mock(LogLine.class);
    Testmark.enable().withOptions(Scale.by(10)).withOptions(LogLine.class, logLine);
    assertTrue(Testmark.isEnabled());
    doAnswer(invocation -> {
      final Scale scale = Testmark.getOptions(Scale.class, Scale.unity());
      assertEquals(10, scale.magnitude());
      return null;
    }).when(r).run();
    
    Testmark.ifEnabled(r);
    verify(r).run();
    verify(logLine).accept(isNotNull());
  }
}
