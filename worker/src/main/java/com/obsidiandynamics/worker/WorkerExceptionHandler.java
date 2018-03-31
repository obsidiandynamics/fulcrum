package com.obsidiandynamics.worker;

@FunctionalInterface
public interface WorkerExceptionHandler {
  void handle(WorkerThread thread, Throwable exception);
}
