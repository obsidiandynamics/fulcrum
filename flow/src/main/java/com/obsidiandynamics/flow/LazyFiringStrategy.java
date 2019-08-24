package com.obsidiandynamics.flow;

public final class LazyFiringStrategy extends FiringStrategy {
  private StatefulConfirmation complete;
  
  public LazyFiringStrategy(AbstractFlow flow, StatefulConfirmation head) {
    super(flow, head);
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
