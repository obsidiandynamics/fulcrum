package com.obsidiandynamics.func;

import static org.junit.Assert.*;

import java.io.*;
import java.util.concurrent.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class ExceptionsTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Exceptions.class);
  }

  private static class TestRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    TestRuntimeException(Throwable cause) { super(cause); }
  }

  @Test
  public void testWrapNormal() {
    Exceptions.wrap(() -> {}, TestRuntimeException::new);
  }

  @Test
  public void testWrapInRuntimeExceptionNormal() {
    final int out = Exceptions.wrap(ExceptionsTest::get42, TestRuntimeException::new);
    assertEquals(42, out);
  }

  private interface AmbiguousGetter {
    Object supply();    // the real method that we want

    void supply(int x); // intended to cause compiler confusion

    void run();         // the real method that we want

    void run(int x);    // intended to cause compiler confusion
  }

  @Test
  public void testWrapAmbiguous() {
    final AmbiguousGetter dataSource = new AmbiguousGetter() {
      @Override
      public Object supply() {
        return null;
      }

      @Override
      public void supply(int x) {}

      @Override
      public void run() {}

      @Override
      public void run(int x) {}
    };

    Exceptions.wrapSupplier(dataSource::supply, TestRuntimeException::new);
    Exceptions.wrapRunnable(dataSource::run, TestRuntimeException::new);
  }

  private static int get42() {
    return 42;
  }

  @Test(expected=TestRuntimeException.class)
  public void testWrapInRuntimeExceptionThrown() {
    Exceptions.wrap(Exceptions.doThrow(IOException::new), TestRuntimeException::new);
  }

  @Test(expected=IOException.class)
  public void testDoThrow() throws IOException {
    Exceptions.doThrow(IOException::new).run();
  }

  @Test
  public void testUnwrap() {
    assertNull(Exceptions.unwrap(ExecutionException.class, null));

    assertNull(Exceptions.unwrap(ExecutionException.class, new ExecutionException("No cause", null)));

    final Exception plainException = new Exception("Simulated");
    assertSame(plainException, Exceptions.unwrap(ExecutionException.class, plainException));

    final ExecutionException executionException = new ExecutionException(plainException);
    assertSame(plainException, Exceptions.unwrap(ExecutionException.class, executionException));

    final IOException ioException = new IOException(new ExecutionException(plainException));
    assertSame(plainException, Exceptions.unwrap(ExecutionException.class, 
                                                 Exceptions.unwrap(IOException.class, 
                                                                   ioException)));
  }
}
