package com.obsidiandynamics.worker;

@FunctionalInterface
public interface WorkerStartup {
  void handle(WorkerThread thread);
}
