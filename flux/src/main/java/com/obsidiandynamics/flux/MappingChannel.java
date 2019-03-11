package com.obsidiandynamics.flux;

import static com.obsidiandynamics.func.Functions.*;

import com.obsidiandynamics.worker.*;

public final class MappingChannel<I, O> implements Channel<I, O> {
  private final EventMapper<? super I, ? extends O> eventMapper;
  
  private final AbstractEmissionContext<O> context = new AbstractEmissionContext<O>() {
    @Override
    public void terminateImpl() {
      MappingChannel.this.terminate();
    }
  };
  
  private final StageCompletionHandlerHolder completionHandlerHolder = new StageCompletionHandlerHolder();
  
  private StageController controller;
  
  private Sink<O> downstream;
  
  private volatile boolean terminating;
  
  public MappingChannel(EventMapper<? super I, ? extends O> eventMapper) {
    this.eventMapper = eventMapper;
  }
  
  public MappingChannel<I, O> onComplete(StageCompletionHandler completionHandler) {
    completionHandlerHolder.setHandler(completionHandler);
    return this;
  }
  
  @Override
  public void assignDownstream(Sink<O> downstream) {
    mustBeNull(this.downstream, illegalState("Downstream stage already assigned"));
    this.downstream = downstream;
  }

  @Override
  public void start(StageController controller) {
    mustExist(controller);
    mustBeNull(this.controller, illegalArgument("Already started"));
    mustExist(downstream, illegalState("No downstream stage assigned"));
    
    this.controller = controller;
  }
  
  @Override
  public void onNext(I next) throws InterruptedException, FluxException {
    if (terminating) return;
    
    eventMapper.apply(context, next);
    while (context.hasNext()) {
      downstream.onNext(context.next());
    }
  }

  @Override
  public Joinable terminate() {
    terminating = true;
    controller.complete(null);
    completionHandlerHolder.fire();
    return this;
  }

  @Override
  public boolean join(long timeoutMillis) throws InterruptedException {
    return true;
  }

  @Override
  public void onUpstreamComplete() {
    mustBeStarted();
    terminate();
  }

  @Override
  public void onDownstreamComplete() {
    mustBeStarted();
    terminate();
  }
  
  private void mustBeStarted() {
    mustExist(controller, illegalState("Channel not started"));
  }
}
