package com.obsidiandynamics.worker;

@FunctionalInterface
public interface WorkerCycle {
  void cycle(WorkerThread thread) throws InterruptedException;
}
