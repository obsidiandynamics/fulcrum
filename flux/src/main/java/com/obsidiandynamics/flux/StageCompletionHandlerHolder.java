package com.obsidiandynamics.flux;

import java.util.concurrent.atomic.*;

public final class StageCompletionHandlerHolder {
  private StageCompletionHandler completionHandler = StageCompletionHandler.nop();
  
  private final AtomicBoolean fired = new AtomicBoolean();
  
  public void setHandler(StageCompletionHandler completionHandler) {
    this.completionHandler = completionHandler;
  }
  
  public void fire() {
    if (fired.compareAndSet(false, true)) {
      completionHandler.onComplete();
    }
  }
}
