package com.obsidiandynamics.flux;

import java.util.concurrent.atomic.*;

final class PipelineCompletionHandlerHolder {
  private PipelineCompletionHandler completionHandler = PipelineCompletionHandler.nop();
  
  private final AtomicBoolean fired = new AtomicBoolean();
  
  void setHandler(PipelineCompletionHandler completionHandler) {
    this.completionHandler = completionHandler;
  }
  
  void fire(Throwable error) {
    if (fired.compareAndSet(false, true)) {
      completionHandler.onComplete(error);
    }
  }
}
