package com.obsidiandynamics.flow;

import java.util.concurrent.atomic.*;

public final class LazyFiringStrategy extends FiringStrategy {
  private FlowConfirmation complete;
  
  public LazyFiringStrategy(ThreadedFlow flow, AtomicReference<FlowConfirmation> tail) {
    super(flow, tail);
  }

  //TODO remove
//  @Override
//  public void cycle(WorkerThread thread) throws InterruptedException {
//    if (current != null) {
//      if (current.isAnchor()) {
//        // skip the anchor
//      } else if (current.isConfirmed()) {
//        flow.removeWithoutCompleting(current.getId());
//        complete = current;
//      } else {
//        if (complete != null) { 
//          flow.complete(complete);
//          complete = null;
//        }
//        
//        Thread.sleep(CYCLE_IDLE_INTERVAL_MILLIS);
//        return;
//      }
//    } else {
//      Thread.sleep(CYCLE_IDLE_INTERVAL_MILLIS);
//    }
//    
//    current = head.next();
//    if (current != null) {
//      head = current;
//    } else if (complete != null) { 
//      flow.complete(complete);
//      complete = null;
//    }
//  }
  
  @Override
  void fire() {
    for (;;) {
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
          return;
        }
      }
      
      current = head.next();
      if (current != null) {
        head = current;
      } else {
        if (complete != null) { 
          flow.complete(complete);
          complete = null;
        }
        return;
      }
    }
  }
}
