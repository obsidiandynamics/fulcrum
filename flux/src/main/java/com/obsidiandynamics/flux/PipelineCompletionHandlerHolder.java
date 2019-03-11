package com.obsidiandynamics.flux;

import java.util.concurrent.atomic.*;

public final class PipelineCompletionHandlerHolder {
  private PipelineCompletionHandler completionHandler = PipelineCompletionHandler.nop();
  
  private final AtomicBoolean fired = new AtomicBoolean();
  
  public void setHandler(PipelineCompletionHandler completionHandler) {
    this.completionHandler = completionHandler;
  }
  
  public void fire(Throwable error) {
    if (fired.compareAndSet(false, true)) {
      completionHandler.onComplete(error);
    }
  }
}
