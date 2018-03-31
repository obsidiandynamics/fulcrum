package com.obsidiandynamics.worker;

import java.io.*;

public final class WorkerThreadBuilder {
  private WorkerOptions options = new WorkerOptions();
  
  private WorkerCycle onCycle;
  
  private WorkerStartup onStartup = t -> {};
  
  private WorkerShutdown onShutdown = (t, x) -> {};
  
  private WorkerExceptionHandler onUncaughtException = SYS_ERR_UNCAUGHT_EXCEPTION_HANDLER;
  
  public static final WorkerExceptionHandler SYS_ERR_UNCAUGHT_EXCEPTION_HANDLER = createPrintStreamUncaughtExceptionHandler(System.err);
  
  public static WorkerExceptionHandler createPrintStreamUncaughtExceptionHandler(PrintStream printStream) {
    return (t, x) -> x.printStackTrace(printStream);
  }
  
  WorkerThreadBuilder() {}

  public WorkerThreadBuilder withOptions(WorkerOptions options) {
    this.options = options;
    return this;
  }

  public WorkerThreadBuilder onCycle(WorkerCycle onCycle) {
    this.onCycle = onCycle;
    return this;
  }
  
  public WorkerThreadBuilder onStartup(WorkerStartup onStartup) {
    this.onStartup = onStartup;
    return this;
  }
  
  public WorkerThreadBuilder onShutdown(WorkerShutdown onShutdown) {
    this.onShutdown = onShutdown;
    return this;
  }
  
  public WorkerThreadBuilder onUncaughtException(WorkerExceptionHandler onUncaughtException) {
    this.onUncaughtException = onUncaughtException;
    return this;
  }
  
  public WorkerThread build() {
    if (onCycle == null) {
      throw new IllegalStateException("onCycle behaviour not set");
    }
    
    return new WorkerThread(options, onCycle, onStartup, onShutdown, onUncaughtException);
  }
  
  public WorkerThread buildAndStart() {
    final WorkerThread thread = build();
    thread.start();
    return thread;
  }
}
