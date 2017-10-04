package com.obsidiandynamics.shell;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 *  Builds the process definition to be executed within a shell.
 */
public final class ProcBuilder {
  private Shell shell = new BourneShell();
  
  private String[] command;
  
  private Process proc;
  
  ProcBuilder() {}

  /**
   *  Assigns a shell.
   *  
   *  @param shell The shell to use.
   *  @return The current {@link ProcBuilder} instance for chaining.
   */
  public ProcBuilder withShell(Shell shell) {
    ensureNotExecuted();
    this.shell = shell;
    return this;
  }

  /**
   *  Executes the given command in a shell.
   *  
   *  @param command The command fragments to execute.
   *  @return The current {@link ProcBuilder} instance for chaining.
   */
  public ProcBuilder execute(String... command) {
    ensureNotExecuted();
    this.command = command;
    startProcess();
    return this;
  }
  
  private void startProcess() {
    final String[] preparedCommand = shell.prepare(command);
    final ProcessBuilder pb = new ProcessBuilder(preparedCommand);
    pb.redirectErrorStream(true);
    try {
      proc = pb.start();
    } catch (IOException e) {
      throw new ProcessException("Error executing prepared command " + Arrays.asList(preparedCommand), e);
    }
  }
  
  private void ensureNotExecuted() {
    if (proc != null) throw new IllegalStateException("A process has already been executed");
  }
  
  private void ensureExecuted() {
    if (proc == null) throw new IllegalStateException("No process has yet been executed");
  }

  /**
   *  Pipes stdout an stderr to the given sink.
   *  
   *  @param sink The sink to receive process output.
   *  @return The current {@link ProcBuilder} instance for chaining.
   */
  public ProcBuilder pipeTo(Consumer<String> sink) {
    ensureExecuted();
    try (InputStream in = proc.getInputStream()) {
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

        if (sink != null) {
          sink.accept(output);
        }
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
    ensureExecuted();
    try {
      return proc.waitFor();
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
    ensureExecuted();
    try {
      final long started = System.currentTimeMillis();
      proc.waitFor(timeout, unit);
      final long took = System.currentTimeMillis() - started;
      if (proc.isAlive()) {
        throw new TimeoutException(String.format("Process is still alive after %,d ms", took));
      } else {
        return proc.exitValue();
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
    ensureExecuted();
    return proc;
  }
}
