package com.obsidiandynamics.flux;

public interface Sink<I> extends DiscreteStage {
  void onNext(I next) throws InterruptedException, FluxException;
  
  void onUpstreamComplete();
}
