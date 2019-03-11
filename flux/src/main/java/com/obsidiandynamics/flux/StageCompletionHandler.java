package com.obsidiandynamics.flux;

@FunctionalInterface
public interface StageCompletionHandler {
  void onComplete();
  
  static StageCompletionHandler nop() { return () -> {}; }
}
