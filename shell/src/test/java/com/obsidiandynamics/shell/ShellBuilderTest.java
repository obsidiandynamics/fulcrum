package com.obsidiandynamics.shell;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.concurrent.*;

import org.junit.*;

public final class ShellBuilderTest {
  private ProcessExecutor executor;
  
  private ShellBuilder builder;

  @Before
  public void before() {
    executor = mock(ProcessExecutor.class);
    doCallRealMethod().when(executor).tryRun(any());
    doCallRealMethod().when(executor).canTryRun(any());
    builder = Shell.builder().withExecutor(executor);
  }

  @Test(expected=IllegalStateException.class)
  public void testEnsureNotExecuted() throws IOException {
    when(executor.run(any())).thenReturn(mock(Process.class));
    builder.execute("test");
    builder.withShell(null);
  }

  @Test(expected=IllegalStateException.class)
  public void testEnsureExecuted() throws IOException {
    builder.pipeTo(s -> {});
  }

  @Test
  public void testWithShell() throws IOException {
    final Shell shell = mock(Shell.class);
    builder.withShell(shell);
    final Process proc = mock(Process.class);
    when(executor.run(any())).thenReturn(proc);
    builder.execute("test");
    verify(shell).prepare(eq("test"));
  }
  
  @Test
  public void testPipeTo() throws IOException {
    final Process proc = mock(Process.class);
    when(executor.run(any())).thenReturn(proc);
    builder.execute();
    assertEquals(proc, builder.getProcess());
    final String output = "test stream";
    final InputStream in = new ByteArrayInputStream(output.getBytes());
    when(proc.getInputStream()).thenReturn(in);
    final StringBuilder sink = new StringBuilder();
    builder.pipeTo(sink::append);
    assertEquals(output, sink.toString());
  }
  
  @Test(expected=ProcessException.class)
  public void testPipeToException() throws IOException {
    final Process proc = mock(Process.class);
    when(executor.run(any())).thenReturn(proc);
    builder.execute();
    final InputStream in = mock(InputStream.class);
    when(proc.getInputStream()).thenReturn(in);
    when(in.read()).thenThrow(new IOException("boom"));
    builder.pipeTo(s -> {});
  }

  @Test(expected=IllegalStateException.class)
  public void testExecuteNullProcess() throws IOException {
    builder.execute("test");
  }
  
  @Test(expected=ProcessException.class)
  public void testExecuteError() throws IOException {
    when(executor.run(any())).thenThrow(new IOException("boom"));
    builder.execute();
  }
  
  @Test
  public void testAwaitUnbounded() throws IOException, InterruptedException {
    final Process proc = mock(Process.class);
    when(executor.run(any())).thenReturn(proc);
    final int exitCode = 3;
    when(proc.waitFor()).thenReturn(exitCode);
    assertEquals(exitCode, builder.execute().await());
  }
  
  @Test
  public void testAwaitUnboundedInterrupted() throws IOException, InterruptedException {
    final Process proc = mock(Process.class);
    when(executor.run(any())).thenReturn(proc);
    when(proc.waitFor()).then(invocation -> {
      Thread.currentThread().interrupt();
      Thread.sleep(1);
      return null;
    });
    assertEquals(-1, builder.execute().await());
    assertTrue(Thread.interrupted());
  }
  
  @Test
  public void testAwaitBounded() throws IOException, InterruptedException, TimeoutException {
    final Process proc = mock(Process.class);
    when(executor.run(any())).thenReturn(proc);
    final int exitCode = 3;
    when(proc.waitFor(isA(long.class), any())).thenReturn(true);
    when(proc.exitValue()).thenReturn(exitCode);
    assertEquals(exitCode, builder.execute().await(1, TimeUnit.MILLISECONDS));
  }
  
  @Test(expected=TimeoutException.class)
  public void testAwaitBoundedTimeout() throws IOException, InterruptedException, TimeoutException {
    final Process proc = mock(Process.class);
    when(executor.run(any())).thenReturn(proc);
    when(proc.waitFor(isA(long.class), any())).thenReturn(false);
    builder.execute().await(1, TimeUnit.MILLISECONDS);
  }
  
  @Test
  public void testAwaitBoundedInterrupted() throws IOException, InterruptedException, TimeoutException {
    final Process proc = mock(Process.class);
    when(executor.run(any())).thenReturn(proc);
    when(proc.waitFor(isA(long.class), any())).then(invocation -> {
      Thread.currentThread().interrupt();
      Thread.sleep(1);
      return null;
    });
    assertEquals(-1, builder.execute().await(1, TimeUnit.SECONDS));
    assertTrue(Thread.interrupted());
  }
}
