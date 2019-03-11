package com.obsidiandynamics.flux;

@FunctionalInterface
public interface PipelineCompletionHandler {
  void onComplete(Throwable error);
  
  static PipelineCompletionHandler nop() { return __ -> {}; }
}
