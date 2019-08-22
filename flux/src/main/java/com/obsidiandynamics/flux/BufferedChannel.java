package com.obsidiandynamics.flux;

import static com.obsidiandynamics.func.Functions.*;

import java.util.*;

import com.obsidiandynamics.worker.*;

public final class BufferedChannel<E> implements Channel<E, E> {
  /** The default queue poll interval, in milliseconds. */
  private static final int DEF_QUEUE_POLL_INTERVAL = 10;
  
  private static final WorkerOptions DEF_WORKER_OPTIONS = new WorkerOptions().withName(BufferedChannel.class, "driver").daemon();
  
  private final BackingQueue<E> queue;
  
  private WorkerOptions workerOptions = DEF_WORKER_OPTIONS;
  
  private int queuePollInterval = DEF_QUEUE_POLL_INTERVAL;
  
  private StageController controller;
  
  private WorkerThread thread;
  
  private Sink<E> downstream;
  
  private volatile boolean draining;
  
  public BufferedChannel(BackingQueueFactory queueFactory, int capacity) {
    queue = queueFactory.create(capacity);
  }
  
  public BufferedChannel<E> withWorkerOptions(WorkerOptions workerOptions) {
    mustExist(workerOptions, "Worker options cannot be null");
    this.workerOptions = workerOptions;
    return this;
  }
  
  public BufferedChannel<E> withQueuePollInterval(int queuePollIntervalMillis) {
    mustBeGreaterOrEqual(queuePollIntervalMillis, 0, illegalArgument("Poll interval must be non-negative"));
    this.queuePollInterval = queuePollIntervalMillis;
    return this;
  }

  @Override
  public void assignDownstream(Sink<E> downstream) {
    mustBeNull(this.downstream, illegalState("Downstream stage already assigned"));
    this.downstream = downstream;
  }

  @Override
  public void start(StageController controller) { // lgtm [java/duplicate-method]
    mustExist(controller);
    mustBeNull(this.controller, illegalArgument("Already started"));
    mustExist(downstream, illegalState("No downstream stage assigned"));
    
    this.controller = controller;
    
    thread = WorkerThread
        .builder()
        .withOptions(workerOptions)
        .onCycle(this::onCycle)
        .onShutdown(this::onShutdown)
        .onUncaughtException(WorkerExceptionHandler.nop())
        .build();
    thread.start();
  }
  
  private void onCycle(WorkerThread thread) throws InterruptedException {
    final boolean wasDraining = draining;
    final E next = queue.poll(queuePollInterval);
    if (next != null) {
      try {
        downstream.onNext(next);
      } catch (FluxException e) {
        throw new RuntimeWorkerException(e);
      }
    } else if (wasDraining) {
      thread.terminate();
    }
  }
  
  private void onShutdown(WorkerThread thread, Throwable exception) {
    queue.dispose();
    controller.complete(RuntimeWorkerException.unpackConditional(exception));
  }

  @Override
  public void onNext(E event) throws InterruptedException {
    mustBeStarted();
    queue.put(event);
  }

  @Override
  public Joinable terminate() {
    ifPresentVoid(thread, WorkerThread::terminate);
    return this;
  }

  @Override
  public boolean join(long timeoutMillis) throws InterruptedException {
    return Joiner.blank().add(Optional.ofNullable(thread)).join(timeoutMillis);
  }

  @Override
  public void onUpstreamComplete() {
    mustBeStarted();
    draining = true;
  }

  @Override
  public void onDownstreamComplete() {
    mustBeStarted();
    thread.terminate();
  }
  
  private void mustBeStarted() {
    mustExist(controller, illegalState("Channel not started"));
  }
}
