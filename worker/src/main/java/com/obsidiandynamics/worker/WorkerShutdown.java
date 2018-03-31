package com.obsidiandynamics.worker;

@FunctionalInterface
public interface WorkerShutdown {
  void handle(WorkerThread thread, Throwable exception);
}
