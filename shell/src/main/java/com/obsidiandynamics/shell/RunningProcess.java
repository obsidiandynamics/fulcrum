package com.obsidiandynamics.shell;

import java.io.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 *  Wraps a running {@link Process}.
 */
public final class RunningProcess {
  private final String[] preparedCommand;
  
  private final Process process;

  RunningProcess(String[] preparedCommand, Process process) {
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
   *  Pipes stdout an stderr to the given sink.
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
