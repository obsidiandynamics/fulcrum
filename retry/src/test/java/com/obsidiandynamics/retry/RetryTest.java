package com.obsidiandynamics.retry;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;
import com.obsidiandynamics.func.*;

public final class RetryTest {
  private static class TestRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    TestRuntimeException(String m) { super(m); }
  }
  
  private static CheckedRunnable<RuntimeException> failFor(int attempts) {
    final AtomicInteger calls = new AtomicInteger();
    return () -> {
      final int call = calls.incrementAndGet();
      if (call <= attempts) throw new TestRuntimeException("Failing on attempt " + call);
    };
  }
  
  @Test
  public void testIsAMatcher() {
    final Predicate<Throwable> matcher = Retry.isA(TestRuntimeException.class);
    assertTrue(matcher.test(new TestRuntimeException("")));
    assertFalse(matcher.test(new IOException()));
    Assertions.assertToStringOverride(matcher);
  }
  
  @Test
  public void testHasCauseThatMatcher() {
    final Predicate<Throwable> matcher = Retry.hasCauseThat(Retry.isA(IOException.class));
    assertFalse(matcher.test(new IllegalStateException()));
    assertTrue(matcher.test(new IllegalStateException(new IOException())));
    assertFalse(matcher.test(new IOException()));
    Assertions.assertToStringOverride(matcher);
  }
  
  @Test
  public void testSuccess() {
    final ExceptionHandler faultHandler = mock(ExceptionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    final int answer = new Retry()
        .withExceptionMatcher(Retry.isA(TestRuntimeException.class))
        .withAttempts(1)
        .withFaultHandler(faultHandler)
        .withErrorHandler(errorHandler)
        .run(() -> 42);
    assertEquals(42, answer);
    verifyNoMoreInteractions(faultHandler);
    verifyNoMoreInteractions(errorHandler);
  }
  
  @Test
  public void testSuccessAfterOneFailure() {
    final ExceptionHandler faultHandler = mock(ExceptionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    new Retry()
    .withExceptionMatcher(Retry.isA(TestRuntimeException.class))
    .withBackoff(0)
    .withAttempts(2)
    .withFaultHandler(faultHandler)
    .withErrorHandler(errorHandler)
    .run(failFor(1));
    
    verify(faultHandler).onException(eq("Fault (attempt #1 of 2): retrying in 0 ms"), isA(TestRuntimeException.class));
    verifyNoMoreInteractions(errorHandler);
  }
  
  @Test
  public void testFailureAndInterrupt() {
    final ExceptionHandler faultHandler = mock(ExceptionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    try {
      Thread.currentThread().interrupt();
      new Retry()
      .withExceptionMatcher(Retry.isA(TestRuntimeException.class))
      .withBackoff(0)
      .withAttempts(2)
      .withFaultHandler(faultHandler)
      .withErrorHandler(errorHandler)
      .run(failFor(1));
      fail("Did not throw expected exception");
    } catch (TestRuntimeException e) {
      assertTrue(Thread.interrupted());
      verify(faultHandler).onException(eq("Fault (attempt #1 of 2): retrying in 0 ms"), isA(TestRuntimeException.class));
      verify(errorHandler).onException(eq("Fault (attempt #1 of 2): aborting due to interrupt"), isA(TestRuntimeException.class));
    } finally {
      Thread.interrupted();
    }
  }
  
  @Test(expected=TestRuntimeException.class)
  public void testFailure() {
    final ExceptionHandler faultHandler = mock(ExceptionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    try {
      new Retry()
      .withExceptionMatcher(Retry.isA(TestRuntimeException.class))
      .withBackoff(0).withAttempts(2)
      .withFaultHandler(faultHandler)
      .withErrorHandler(errorHandler)
      .run(failFor(2));
    } finally {
      verify(faultHandler).onException(eq("Fault (attempt #1 of 2): retrying in 0 ms"), isA(TestRuntimeException.class));
      verify(errorHandler).onException(eq("Fault (attempt #2 of 2): aborting"), isA(TestRuntimeException.class));
    }
  }
  
  private static int throwCheckedException() throws IOException {
    throw new IOException("test exception");
  }
  
  @Test(expected=IOException.class)
  public void testUncaughtCheckedException() throws Exception {
    final ExceptionHandler faultHandler = mock(ExceptionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    final CheckedSupplier<Integer, Exception> supplier = RetryTest::throwCheckedException;
    try {
      new Retry()
      .withExceptionMatcher(Retry.isA(TestRuntimeException.class))
      .withBackoff(0)
      .withAttempts(1)
      .withFaultHandler(faultHandler)
      .withErrorHandler(errorHandler)
      .run(supplier);
    } finally {
      verifyNoMoreInteractions(faultHandler);
      verifyNoMoreInteractions(errorHandler);
    }
  }
  
  @Test(expected=InterruptedException.class)
  public void testUncaughtInterruptedException() throws InterruptedException {
    final ExceptionHandler faultHandler = mock(ExceptionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    final CheckedSupplier<Integer, InterruptedException> supplier = () -> { throw new InterruptedException("test"); };
    try {
      new Retry()
      .withExceptionMatcher(Retry.isA(TestRuntimeException.class))
      .withBackoff(0)
      .withAttempts(1)
      .withFaultHandler(faultHandler)
      .withErrorHandler(errorHandler)
      .run(supplier);
    } finally {
      verifyNoMoreInteractions(faultHandler);
      verifyNoMoreInteractions(errorHandler);
      assertFalse(Thread.interrupted());
    }
  }
  
  @Test(expected=IllegalStateException.class) 
  public void testUncaughtRuntimeException() {
    final ExceptionHandler faultHandler = mock(ExceptionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    new Retry()
    .withExceptionMatcher(Retry.isA(TestRuntimeException.class))
    .withFaultHandler(faultHandler)
    .withErrorHandler(errorHandler)
    .run(() -> {
      throw new IllegalStateException();
    });
    verifyNoMoreInteractions(faultHandler);
    verifyNoMoreInteractions(errorHandler);
  }
  
  @Test
  public void testConfig() {
    final Class<? extends RuntimeException> exceptionClass = TestRuntimeException.class;
    final int attempts = 10;
    final int backoffMillis = 20;
    final ExceptionHandler faultHandler = ExceptionHandler.nop();
    final ExceptionHandler errorHandler = ExceptionHandler.nop();
    
    final Retry r = new Retry()
        .withExceptionMatcher(Retry.isA(exceptionClass))
        .withAttempts(attempts)
        .withBackoff(backoffMillis)
        .withFaultHandler(faultHandler)
        .withErrorHandler(errorHandler);
    Assertions.assertToStringOverride(r);
  }
}
