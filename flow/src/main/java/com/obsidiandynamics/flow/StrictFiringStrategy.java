package com.obsidiandynamics.flow;

public final class StrictFiringStrategy extends FiringStrategy {
  public StrictFiringStrategy(AbstractFlow flow, StatefulConfirmation head) {
    super(flow, head);
  }

  //TODO remove
//  @Override
//  public void cycle(WorkerThread thread) throws InterruptedException {
//    if (current != null) {
//      if (current.isAnchor()) {
//        // skip the anchor
//      } else if (current.isConfirmed()) {
//        flow.complete(current);
//      } else {
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
//    }
//  }
  
  @Override
  void fire() {
    for (;;) {
      if (current != null) {
        if (current.isAnchor()) {
          // skip the anchor
        } else if (current.isConfirmed()) {
          flow.complete(current);
        } else {
          return;
        }
      }
      
      current = head.next();
      if (current != null) {
        head = current;
      } else {
        return;
      }
    }
  }
}
