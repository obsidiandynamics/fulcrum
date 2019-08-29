package com.obsidiandynamics.worker;

import static com.obsidiandynamics.func.Functions.*;

public final class WorkerThreadBuilder {
  private WorkerOptions options = new WorkerOptions();
  
  private WorkerCycle onCycle;
  
  private WorkerStartup onStartup = t -> {};
  
  private WorkerShutdown onShutdown = (t, x) -> {};
  
  private WorkerExceptionHandler onUncaughtException = SYS_ERR_UNCAUGHT_EXCEPTION_HANDLER;
  
  public static final WorkerExceptionHandler SYS_ERR_UNCAUGHT_EXCEPTION_HANDLER = WorkerExceptionHandler.forPrintStream(System.err);
  
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
    mustExist(onStartup, "On-startup handler cannot be null");
    mustExist(onShutdown, "On-shutdown handler cannot be null");
    mustExist(onCycle, "On-cycle handler cannot be null");
    mustExist(options, "Options cannot be null");
    mustExist(onUncaughtException, "Uncaught exception handler be null");
    return new WorkerThread(options, onCycle, onStartup, onShutdown, onUncaughtException);
  }
  
  public WorkerThread buildAndStart() {
    final WorkerThread thread = build();
    thread.start();
    return thread;
  }
}
