package com.obsidiandynamics.flux;

public interface Emitter<O> extends DiscreteStage {
  void assignDownstream(Sink<O> downstream);
  
  void onDownstreamComplete();
}
