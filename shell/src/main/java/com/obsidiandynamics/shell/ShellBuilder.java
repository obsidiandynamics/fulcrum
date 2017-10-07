package com.obsidiandynamics.shell;

import java.io.*;
import java.util.*;

/**
 *  Builds the process definition to be executed within a shell.
 */
public final class ShellBuilder {
  private Shell shell = Shell.getDefault();
  
  private ProcessExecutor executor = ProcessExecutor.getDefault();
  
  ShellBuilder() {}

  /**
   *  Assigns a shell.
   *  
   *  @param shell The shell to use.
   *  @return The current {@link ShellBuilder} instance for chaining.
   */
  public ShellBuilder withShell(Shell shell) {
    this.shell = shell;
    return this;
  }
  
  /**
   *  Assigns a custom executor, in place of the default {@link DefaultProcessExecutor}.
   *  
   *  @param executor The executor to set.
   *  @return The current {@link ShellBuilder} instance for chaining.
   */
  public ShellBuilder withExecutor(ProcessExecutor executor) {
    this.executor = executor;
    return this;
  }

  /**
   *  Executes the given command in a shell.
   *  
   *  @param command The command fragments to execute.
   *  @return A new {@link RunningProcess} instance.
   */
  public RunningProcess execute(String... command) {
    final String[] preparedCommand = shell.prepare(command);
    final Process process;
    try {
      process = executor.run(preparedCommand);
      if (process == null) throw new IllegalStateException("Executor returned a null process");
    } catch (IOException e) {
      throw new ProcessException("Error executing prepared command " + Arrays.asList(preparedCommand), e);
    }
    
    return new RunningProcess(preparedCommand, process);
  }
}
