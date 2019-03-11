package com.obsidiandynamics.flux;

import static com.obsidiandynamics.func.Functions.*;

import java.util.*;

import com.obsidiandynamics.worker.*;

public final class SupplierEmitter<E> implements Emitter<E> {
  private static final WorkerOptions DEF_WORKER_OPTIONS = new WorkerOptions().withName(SupplierEmitter.class, "driver").daemon();
  
  private final EventSupplier<? extends E> eventSupplier;
  
  private final AbstractEmissionContext<E> context = new AbstractEmissionContext<E>() {
    @Override
    public void terminateImpl() {
      SupplierEmitter.this.terminate();
    }
  };
  
  private WorkerOptions workerOptions = DEF_WORKER_OPTIONS;

  private final StageCompletionHandlerHolder completionHandlerHolder = new StageCompletionHandlerHolder();
  
  private StageController controller;
  
  private WorkerThread thread;
  
  private Sink<E> downstream;
  
  public SupplierEmitter(EventSupplier<? extends E> eventSupplier) {
    this.eventSupplier = mustExist(eventSupplier, "Event supplier cannot be null");
  }

  public SupplierEmitter<E> withWorkerOptions(WorkerOptions workerOptions) {
    mustExist(workerOptions, "Worker options cannot be null");
    this.workerOptions = workerOptions;
    return this;
  }
  
  public SupplierEmitter<E> onComplete(StageCompletionHandler completionHandler) {
    completionHandlerHolder.setHandler(completionHandler);
    return this;
  }

  @Override
  public void assignDownstream(Sink<E> downstream) {
    mustBeNull(this.downstream, illegalState("Downstream stage already assigned"));
    this.downstream = downstream;
  }
  
  @Override
  public void start(StageController controller) {
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
    try {
      eventSupplier.get(context);
      while (context.hasNext()) {
        downstream.onNext(context.next());
      }
    } catch (FluxException e) {
      throw new RuntimeWorkerException(e);
    }
  }
  
  private void onShutdown(WorkerThread thread, Throwable exception) {
    controller.complete(RuntimeWorkerException.unpackConditional(exception));
    completionHandlerHolder.fire();
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
  public void onDownstreamComplete() {
    mustBeStarted();
    terminate();
  }

  private void mustBeStarted() {
    mustExist(controller, illegalState("Emitter not started"));
  }
}
