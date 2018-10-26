package com.obsidiandynamics.io;

import static org.mockito.Mockito.*;

import java.io.*;

import org.hamcrest.core.*;
import org.junit.*;
import org.junit.rules.*;

import com.obsidiandynamics.assertion.*;

public final class IOTest {
  @Rule
  public final ExpectedException expectedException = ExpectedException.none();
  
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(IO.class);
  }
  
  @Test
  public void testCloseUnchecked() throws IOException {
    final Closeable closeable = mock(Closeable.class);
    IO.closeUnchecked(closeable);
    verify(closeable).close();
  }
  
  @Test
  public void testCloseUncheckedWithException() throws IOException {
    final Closeable closeable = mock(Closeable.class);
    final IOException exception = new IOException("Simulated");
    doThrow(exception).when(closeable).close();
    
    expectedException.expect(RuntimeIOException.class);
    expectedException.expectCause(Is.is(exception));
    IO.closeUnchecked(closeable);
  }
}
