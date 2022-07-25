package com.obsidiandynamics.flux;

import static com.obsidiandynamics.func.Functions.*;

import com.obsidiandynamics.worker.*;

public final class ConsumerSink<E> implements Sink<E> {
  private final StageContext context = ConsumerSink.this::terminate;
  
  private final EventConsumer<? super E> eventConsumer;

  private final StageCompletionHandlerHolder completionHandlerHolder = new StageCompletionHandlerHolder();
  
  private StageController controller;
  
  private volatile boolean terminating;
  
  public ConsumerSink(EventConsumer<? super E> eventConsumer) {
    this.eventConsumer = mustExist(eventConsumer, "Event consumer cannot be null");
  }
  
  public ConsumerSink<E> onComplete(StageCompletionHandler completionHandler) {
    completionHandlerHolder.setHandler(completionHandler);
    return this;
  }

  @Override
  public void start(StageController controller) {
    mustExist(controller);
    mustBeNull(this.controller, illegalArgument("Already started"));
    this.controller = controller;
  }

  @Override
  public Joinable terminate() {
    terminating = true;
    controller.complete(null);
    completionHandlerHolder.fire();
    return this;
  }

  @Override
  public boolean join(long timeoutMillis) {
    return true;
  }

  @Override
  public void onNext(E next) throws FluxException, InterruptedException {
    if (terminating) return;
    
    eventConsumer.accept(context, next);
  }

  @Override
  public void onUpstreamComplete() {
    mustBeStarted();
    terminate();
  }
  
  private void mustBeStarted() {
    mustExist(controller, illegalState("Sink not started"));
  }
}
