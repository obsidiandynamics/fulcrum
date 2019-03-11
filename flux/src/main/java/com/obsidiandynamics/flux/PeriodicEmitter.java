package com.obsidiandynamics.flux;

import static com.obsidiandynamics.func.Functions.*;
import static com.obsidiandynamics.threads.Chrono.*;

import java.util.*;

import com.obsidiandynamics.threads.*;
import com.obsidiandynamics.worker.*;

public final class PeriodicEmitter<E> implements Emitter<E> {
  private static final WorkerOptions DEF_WORKER_OPTIONS = new WorkerOptions().withName(PeriodicEmitter.class, "driver").daemon();
  
  private final Rate rate;
  
  private final EventSupplier<? extends E> eventSupplier;
  
  private final AbstractEmissionContext<E> context = new AbstractEmissionContext<E>() {
    @Override
    public void terminateImpl() {
      PeriodicEmitter.this.terminate();
    }
  };

  private final long expectedEvents;
  
  private WorkerOptions workerOptions = DEF_WORKER_OPTIONS;
  
  private final StageCompletionHandlerHolder completionHandlerHolder = new StageCompletionHandlerHolder();
  
  private StageController controller;
  
  private WorkerThread thread;
  
  private Sink<E> downstream;
  
  private long startTime;
  
  private long stopTime;
  
  private long emittedEvents;
  
  public PeriodicEmitter(Rate rate, EventSupplier<? extends E> eventSupplier) {
    this.rate = mustExist(rate, "Rate cannot be null");
    this.eventSupplier = mustExist(eventSupplier, "Event supplier cannot be null");
    expectedEvents = rate.computeTotal();
  }

  public PeriodicEmitter<E> withWorkerOptions(WorkerOptions workerOptions) {
    mustExist(workerOptions, "Worker options cannot be null");
    this.workerOptions = workerOptions;
    return this;
  }
  
  public PeriodicEmitter<E> onComplete(StageCompletionHandler completionHandler) {
    completionHandlerHolder.setHandler(completionHandler);
    return this;
  }

  @Override
  public void assignDownstream(Sink<E> downstream) {
    mustBeNull(this.downstream, illegalState("Downstream stage already assigned"));
    this.downstream = downstream;
  }
  
  public Rate getRate() {
    return rate;
  }
  
  public long getEmittedEvents() {
    return emittedEvents;
  }
  
  public long getExpectedEvents() {
    return expectedEvents;
  }
  
  public double getElapsedTime() {
    return (double) (stopTime - startTime) / Chrono.NANOS_IN_SECOND;
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
        .onStartup(this::onStartup)
        .onCycle(this::onCycle)
        .onShutdown(this::onShutdown)
        .onUncaughtException(WorkerExceptionHandler.nop())
        .build();
    thread.start();
  }
  
  private void onStartup(WorkerThread thread) {
    startTime = System.nanoTime();
  }
  
  private void onCycle(WorkerThread thread) throws InterruptedException {
    try {
      final double elapsedSeconds = getElapsedSeconds(System.nanoTime() - startTime);
      final long totalEventsRequired = Math.min(rate.computeVolume(elapsedSeconds), expectedEvents);
      final int eventBacklog = capIntRange(totalEventsRequired - emittedEvents);
      
      context.setLimit(eventBacklog);
      while (context.remainingCapacity() > 0 && ! context.isTerminated()) {
        eventSupplier.get(context);
        while (context.hasNext()) {
          downstream.onNext(context.next());
          context.decrementLimit();
        }
      }
      emittedEvents += eventBacklog;
      
      final double updatedElapsedSeconds = getElapsedSeconds(System.nanoTime() - startTime);
      final double currentRate = rate.computeRate(updatedElapsedSeconds);
      
      if (emittedEvents == expectedEvents) {
        terminate();
      } else {
        Chrono.getDefault().parkSeconds(1d / currentRate);
      }
    } catch (FluxException e) {
      throw new RuntimeWorkerException(e);
    }
  }
  
  private static int capIntRange(long value) {
    return (int) Math.min(value, Integer.MAX_VALUE);
  }
  
  private double getElapsedSeconds(long elapsedNanos) {
    return (double) elapsedNanos / NANOS_IN_SECOND;
  }
  
  private void onShutdown(WorkerThread thread, Throwable exception) {
    controller.complete(RuntimeWorkerException.unpackConditional(exception));
    completionHandlerHolder.fire();
    stopTime = System.nanoTime();
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
