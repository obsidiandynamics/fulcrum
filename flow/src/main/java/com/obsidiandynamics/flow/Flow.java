package com.obsidiandynamics.flow;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import com.obsidiandynamics.worker.*;

public final class Flow implements Terminable, Joinable {
  /** Atomically assigns sequence numbers for thread naming. */
  private static final AtomicInteger nextThreadNo = new AtomicInteger();
  
  private final WorkerThread executor;
  
  private final AtomicReference<FlowConfirmation> tail = new AtomicReference<>(FlowConfirmation.anchor(this));
  
  private final ConcurrentHashMap<Object, FlowConfirmation> confirmations = new ConcurrentHashMap<>();
  
  public Flow(FiringStrategy.Factory completionStrategyFactory) {
    this(completionStrategyFactory, Flow.class.getSimpleName() + "-" + nextThreadNo.getAndIncrement());
  }
  
  public Flow(FiringStrategy.Factory firingStrategyFactory, String threadName) {
    executor = WorkerThread.builder()
        .withOptions(new WorkerOptions().daemon().withName(threadName))
        .onCycle(firingStrategyFactory.create(this, tail))
        .buildAndStart();
  }
  
  public FlowConfirmation begin(Object id, Runnable task) {
    final FlowConfirmation confirmation = confirmations.computeIfAbsent(id, __ -> {
      final FlowConfirmation newConfirmation = new FlowConfirmation(id, task);
      newConfirmation.appendTo(tail);
      return newConfirmation;
    });
    confirmation.addRequest();
    return confirmation;
  }
  
  void removeWithoutCompleting(Object id) {
    confirmations.remove(id);
  }
  
  void complete(FlowConfirmation confirmation) {
    confirmations.remove(confirmation.getId());
    confirmation.getTask().run();
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
  
  public Map<Object, FlowConfirmation> getPendingConfirmations() {
    return Collections.unmodifiableMap(confirmations);
  }
  
  @Override
  public boolean join(long timeoutMillis) throws InterruptedException {
    return executor.join(timeoutMillis);
  }
}
