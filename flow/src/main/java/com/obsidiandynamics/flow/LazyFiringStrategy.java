package com.obsidiandynamics.flow;

public final class LazyFiringStrategy extends FiringStrategy {
  public LazyFiringStrategy(AbstractFlow flow, StatefulConfirmation head) {
    super(flow, head);
  }

  @Override
  void fire() {
    StatefulConfirmation toBeCompleted = null;
    
    for (;;) {
      if (current != null) {
        if (current.isAnchor()) {
          // skip the anchor
        } else if (current.isConfirmed()) {
          flow.removeWithoutCompleting(current.getId());
          toBeCompleted = current;
        } else {
          if (toBeCompleted != null) { 
            flow.complete(toBeCompleted);
          }
          return;
        }
      }
      
      current = head.next();
      if (current != null) {
        head = current;
      } else {
        if (toBeCompleted != null) { 
          flow.complete(toBeCompleted);
        }
        return;
      }
    }
  }
}
