package com.obsidiandynamics.shell;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.*;

import org.junit.*;

public final class ProcessExecutorTest {
  private ProcessExecutor executor;
  
  @Before
  public void before() {
    executor = mock(ProcessExecutor.class);
    doCallRealMethod().when(executor).tryRun(any());
    doCallRealMethod().when(executor).canTryRun(any());
  }
  
  @Test
  public void testTryRunSuccess() throws IOException {
    when(executor.run(isA(String.class))).thenReturn(mock(Process.class));
    assertNotNull(executor.tryRun("foo"));
    assertTrue(executor.canTryRun("foo"));
  }
  
  @Test
  public void testTryRunFailure() throws IOException {
    final Exception cause = new IOException("Boom");
    when(executor.run(isA(String.class))).thenThrow(cause);
    assertNull(executor.tryRun("foo"));
    assertFalse(executor.canTryRun("foo"));
  }
}
