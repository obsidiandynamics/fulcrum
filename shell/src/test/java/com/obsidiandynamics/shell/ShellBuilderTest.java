package com.obsidiandynamics.shell;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.*;
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

  @Test
  public void testWithShell() throws IOException {
    final Shell shell = mock(Shell.class);
    when(shell.prepare(any())).then(invocation -> {
      final String[] command = invocation.getArgument(0);
      final List<String> commandList = new ArrayList<>(Arrays.asList(command));
      commandList.add(0, "shell");
      return commandList.toArray(new String[0]);
    });
    builder.withShell(shell);
    final Process proc = mock(Process.class);
    when(executor.run(any())).thenReturn(proc);
    final RunningProcess rp = builder.execute("test");
    verify(shell).prepare(eq(new String[] { "test" }));
    assertNotNull(rp.getProcess());
    assertArrayEquals(new String[] { "shell", "test" }, rp.getPreparedCommand());
    assertEquals("shell test", rp.getSplicedPreparedCommand());
  }
  
  @Test
  public void testPipeTo() throws IOException {
    final Process proc = mock(Process.class);
    when(executor.run(any())).thenReturn(proc);
    final RunningProcess rp = builder.execute();
    assertEquals(proc, rp.getProcess());
    final String output = "test stream";
    final InputStream in = new ByteArrayInputStream(output.getBytes());
    when(proc.getInputStream()).thenReturn(in);
    final StringBuilder sink = new StringBuilder();
    rp.pipeTo(sink::append);
    assertEquals(output, sink.toString());
  }
  
  @Test
  public void testEcho() throws IOException {
    builder.withShell(NullShell.getIntance());
    final Process proc = mock(Process.class);
    when(executor.run(any())).thenReturn(proc);
    final String command = "command";
    final RunningProcess rp = builder.execute(command);
    assertEquals(proc, rp.getProcess());
    final String output = "test stream";
    final InputStream in = new ByteArrayInputStream(output.getBytes());
    when(proc.getInputStream()).thenReturn(in);
    final StringBuilder sink = new StringBuilder();
    rp.echo(sink::append);
    rp.pipeTo(sink::append);
    assertEquals(command + "\n" + output, sink.toString());
  }
  
  @Test(expected=ProcessException.class)
  public void testPipeToException() throws IOException {
    final Process proc = mock(Process.class);
    when(executor.run(any())).thenReturn(proc);
    final RunningProcess rp = builder.execute();
    final InputStream in = mock(InputStream.class);
    when(proc.getInputStream()).thenReturn(in);
    when(in.read()).thenThrow(new IOException("boom"));
    rp.pipeTo(s -> {});
  }

  @Test(expected=IllegalStateException.class)
  public void testExecuteNullProcess() {
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
  
  @Test
  public void checkInstalledPass() throws IOException {
    final Process proc = mock(Process.class);
    when(executor.run(any())).thenReturn(proc);
    builder.checkInstalled("command");
  }
  
  @Test(expected=NotInstalledError.class)
  public void checkInstalledFail() throws IOException, InterruptedException {
    final Process proc = mock(Process.class);
    when(executor.run(any())).thenReturn(proc);
    final int exitCode = 3;
    when(proc.waitFor()).thenReturn(exitCode);
    builder.checkInstalled("command");
  }
}
