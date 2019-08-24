package com.obsidiandynamics.flow;

import java.util.concurrent.atomic.*;

import com.obsidiandynamics.worker.*;

public final class ThreadedFlow extends AbstractFlow {
  private static final int CYCLE_IDLE_INTERVAL_MILLIS = 1;
  
  /** Atomically assigns sequence numbers for thread naming. */
  private static final AtomicInteger nextThreadNo = new AtomicInteger();
  
  private final WorkerThread executor;
  
  public ThreadedFlow(FiringStrategy.Factory completionStrategyFactory) {
    this(completionStrategyFactory, ThreadedFlow.class.getSimpleName() + "-" + nextThreadNo.getAndIncrement());
  }
  
  public ThreadedFlow(FiringStrategy.Factory firingStrategyFactory, String threadName) {
    super(firingStrategyFactory);
    executor = WorkerThread.builder()
        .withOptions(new WorkerOptions().daemon().withName(threadName))
        .onCycle(this::onCycle)
        .buildAndStart();
  }
  
  private void onCycle(WorkerThread thread) throws InterruptedException {
    firingStrategy.fire();
    Thread.sleep(CYCLE_IDLE_INTERVAL_MILLIS);
  }

  @Override
  void fire() {}

  /**
   *  Terminates the flow, shutting down the worker thread and preventing further 
   *  task executions.
   *  
   *  @return A {@link Joinable} for the caller to wait on.
   */
  @Override
  public Joinable terminate() {
    executor.terminate();
    return this;
  }
  
  @Override
  public boolean join(long timeoutMillis) throws InterruptedException {
    return executor.join(timeoutMillis);
  }
}
