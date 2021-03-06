package com.obsidiandynamics.testmark;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.*;
import org.mockito.*;

import com.obsidiandynamics.assertion.*;
import com.obsidiandynamics.func.*;

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
  public void testEnabledNoName() throws Throwable {
    final CheckedRunnable<?> r = mock(CheckedRunnable.class);
    final LogLine logLine = mock(LogLine.class);
    doCallRealMethod().when(logLine).printf(any(), any());
    Testmark.enable().withOptions(LogLine.class, logLine);
    assertTrue(Testmark.isEnabled());
    
    Testmark.ifEnabled(r);
    verify(r).run();
    verify(logLine).println(isNotNull());
  }
  
  @Test
  public void testEnabledWithName() throws Throwable {
    final CheckedRunnable<?> r = mock(CheckedRunnable.class);
    final LogLine logLine = mock(LogLine.class, Answers.CALLS_REAL_METHODS);
    doCallRealMethod().when(logLine).printf(any(), any());
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
  public void testSysErrExceptionHandler() {
    assertNotNull(Testmark.sysErrExceptionHandler());
  }
  
  @Test
  public void testEnabledWithError() throws Throwable {
    final CheckedRunnable<?> r = mock(CheckedRunnable.class);
    final Exception cause = new Exception("test exception");
    doThrow(cause).when(r).run();
    final LogLine logLine = mock(LogLine.class);
    doCallRealMethod().when(logLine).printf(any(), any());
    final ExceptionHandler exceptionHandler = mock(ExceptionHandler.class);
    Testmark.enable().withOptions(LogLine.class, logLine).withOptions(ExceptionHandler.class, exceptionHandler);
    assertTrue(Testmark.isEnabled());
    
    Testmark.ifEnabled(r);
    verify(r).run();
    verify(logLine).println(isNotNull());
    verify(exceptionHandler).onException(notNull(), eq(cause));
  }
  
  @Test
  public void testEnabledWithScale() throws Throwable {
    final CheckedRunnable<?> r = mock(CheckedRunnable.class);
    final LogLine logLine = mock(LogLine.class);
    doCallRealMethod().when(logLine).printf(any(), any());
    Testmark.enable().withOptions(Scale.by(10)).withOptions(LogLine.class, logLine);
    assertTrue(Testmark.isEnabled());
    doAnswer(invocation -> {
      final Scale scale = Testmark.getOptions(Scale.class, Scale.unity());
      assertEquals(10, scale.magnitude());
      return null;
    }).when(r).run();
    
    Testmark.ifEnabled(r);
    verify(r).run();
    verify(logLine).println(isNotNull());
  }
}
