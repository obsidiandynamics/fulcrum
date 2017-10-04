package com.obsidiandynamics.proc;

import java.util.function.*;

public final class ProcBuilder {
  private Shell shell;
  
  private String command;
  
  private Consumer<String> sink;
  
  
  
  ProcBuilder() {}

  public ProcBuilder withShell(Shell shell) {
    this.shell = shell;
    return this;
  }

  public ProcBuilder withCommand(String command) {
    this.command = command;
    return this;
  }

  public ProcBuilder withSink(Consumer<String> sink) {
    this.sink = sink;
    return this;
  }
  
  
}
