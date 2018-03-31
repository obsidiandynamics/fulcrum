package com.obsidiandynamics.flow;

import java.util.concurrent.atomic.*;

import com.obsidiandynamics.worker.*;

public final class LazyFiringStrategy extends FiringStrategy {
  private FlowConfirmation complete;
  
  public LazyFiringStrategy(Flow flow, AtomicReference<FlowConfirmation> tail) {
    super(flow, tail);
  }

  @Override
  public void cycle(WorkerThread thread) throws InterruptedException {
    if (current != null) {
      if (current.isAnchor()) {
        // skip the anchor
      } else if (current.isConfirmed()) {
        flow.removeWithoutCompleting(current.getId());
        complete = current;
      } else {
        if (complete != null) { 
          flow.complete(complete);
          complete = null;
        }
        
        Thread.sleep(CYCLE_IDLE_INTERVAL_MILLIS);
        return;
      }
    } else {
      Thread.sleep(CYCLE_IDLE_INTERVAL_MILLIS);
    }
    
    current = head.next();
    if (current != null) {
      head = current;
    } else if (complete != null) { 
      flow.complete(complete);
      complete = null;
    }
  }
}
