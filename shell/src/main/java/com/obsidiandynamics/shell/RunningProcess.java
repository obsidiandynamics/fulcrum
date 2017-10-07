package com.obsidiandynamics.shell;

import java.io.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 *  Wraps a running {@link Process}.
 */
public final class RunningProcess {
  private final Shell shell;
  
  private final String[] preparedCommand;
  
  private final Process process;

  RunningProcess(Shell shell, String[] preparedCommand, Process process) {
    this.shell = shell;
    this.preparedCommand = preparedCommand;
    this.process = process;
  }
  
  /**
   *  Obtains the prepared command that was used to execute the process.
   *  
   *  @return The prepared command.
   */
  public String[] getPreparedCommand() {
    return preparedCommand;
  }
  
  /**
   *  Obtains the spliced prepared command. The command fragments are spliced with
   *  a single whitespace character as the delimiter.
   *  
   *  @return The spliced prepared command.
   */
  public String getSplicedPreparedCommand() {
    return CommandTransform.splice(preparedCommand);
  }
  
  /**
   *  Pipes the prepared command into the given {@link Sink}, using the default
   *  transform for the {@link Shell} that was used to launch this process.
   *  
   *  @param sink The sink to pipe the command to.
   *  @return The current {@link RunningProcess} instance for chaining.
   */
  public RunningProcess echo(Sink sink) {
    return echo(sink, shell.getDefaultEcho());
  }
  
  /**
   *  Pipes the prepared command into the given {@link Sink}, transforming the command using
   *  the given {@link CommandTransform} implementation. A single newline character is appended
   *  at the end of the transformed command.
   *  
   *  @param sink The sink to pipe the command to.
   *  @param commandTransform A way of transforming the command prior to piping to the {@code sink}.
   *  @return The current {@link RunningProcess} instance for chaining.
   */
  public RunningProcess echo(Sink sink, CommandTransform commandTransform) {
    sink.accept(commandTransform.apply(getPreparedCommand()));
    sink.accept("\n");
    return this;
  }

  /**
   *  Pipes stdout an stderr to the given sink. This variant allows you to specify
   *  a transform for echoing the prepared command
   *  
   *  @param sink The sink to receive process output.
   *  @return The current {@link RunningProcess} instance for chaining.
   */
  public RunningProcess pipeTo(Sink sink) {
    try (InputStream in = process.getInputStream()) {
      readStream(in, sink);
    } catch (IOException e) {
      throw new ProcessException("Error reading stdout", e);
    }
    return this;
  }
  
  private static String readStream(InputStream is, Consumer<String> sink) throws IOException {
    final Writer writer = new StringWriter();
    final char[] buffer = new char[1024];
    try (Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
      int n;
      while ((n = reader.read(buffer)) != -1) {
        final String output = new String(buffer, 0, n);
        writer.write(buffer, 0, n);
        sink.accept(output);
      }
    }
    return writer.toString();
  }
  
  /**
   *  Awaits the termination of the process, returning the exit code.
   *  
   *  @return The exit code, or {@code -1} if this method was interrupted.
   */
  public int await() {
    try {
      return process.waitFor();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return -1;
    }
  }
  
  /**
   *  Awaits the termination of the process, up to a set maximum amount of
   *  time, returning the exit code if the process has terminated within the
   *  wait period.
   *  
   *  @param timeout The maximum time to wait.
   *  @param unit The time unit of the {@code timeout} argument.
   *  @return The exit code, or {@code -1} if this method was interrupted.
   *  @throws TimeoutException If the process failed to terminate within the wait period.
   */
  public int await(long timeout, TimeUnit unit) throws TimeoutException {
    try {
      final long started = System.currentTimeMillis();
      final boolean completed = process.waitFor(timeout, unit);
      final long took = System.currentTimeMillis() - started;
      if (! completed) {
        throw new TimeoutException(String.format("Process is still alive after %,d ms", took));
      } else {
        return process.exitValue();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return -1;
    }
  }
  
  /**
   *  Obtains the underlying process instance.
   *  
   *  @return The executed {@code Process}.
   */
  public Process getProcess() {
    return process;
  }
}
