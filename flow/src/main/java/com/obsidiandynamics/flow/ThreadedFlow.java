package com.obsidiandynamics.flow;

import java.util.concurrent.atomic.*;

import com.obsidiandynamics.worker.*;

/**
 *  A {@link Flow} implementation that recruits a background thread to
 *  scan elements for completion and perform the necessary dispatch. <p>
 *  
 *  A {@link ThreadedFlow} bears the overhead of a running thread. Applications
 *  should therefore limit the number of active {@link ThreadedFlow}s. <p>
 *  
 *  Due to the context switch between a call to {@link StatefulConfirmation#confirm()}
 *  and the background processing of the dispatch logic, the confirm-to-dispatch
 *  latency is not as low as in the {@link ThreadlessFlow} variant. The latency is
 *  that of a typical wait-notify synchronized handover. <p>
 *  
 *  The principal advantage of this implementation is that the execution of the
 *  dispatch tasks does not block the calling application. This may be particularly
 *  advantageous if the tasks are blocking or otherwise time-consuming. Applications
 *  with lightweight dispatch tasks, or a need to run a large number of independent 
 *  flows may be better served with a {@link ThreadlessFlow}.
 *
 *  @see Flow
 */
public final class ThreadedFlow extends AbstractFlow {
  private static final int CYCLE_WAIT_INTERVAL = 1_000;
  
  /** Atomically assigns sequence numbers for thread naming. */
  private static final AtomicInteger nextThreadNo = new AtomicInteger();
  
  private final WorkerThread executor;
  
  private boolean woken;
  
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
    synchronized (firingStrategy) {
      while (! woken) {
        firingStrategy.wait(CYCLE_WAIT_INTERVAL);
      }
      woken = false;
    }
    firingStrategy.fire();
  }

  @Override
  void fire() {
    synchronized (firingStrategy) {
      woken = true;
      firingStrategy.notify();
    }
  }

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
