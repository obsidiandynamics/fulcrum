package com.obsidiandynamics.flow;

/**
 *  Dispatches the last (most recently added) element in a contiguous sequence of
 *  completed {@link StatefulConfirmation}s within in a {@link Flow} model, additionally
 *  ensuring that the preceding sequences are dispatched before their successors. <p>
 *  
 *  A {@link LazyFiringStrategy} is suitable in those application context where dispatching
 *  an element has the implied effect of dispatching all prior elements.
 *
 *  @see FiringStrategy
 *  @see Flow
 */
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
          flow.removeWithoutDispatching(current.getId());
          toBeCompleted = current;
        } else {
          if (toBeCompleted != null) { 
            flow.dispatch(toBeCompleted);
          }
          return;
        }
      }
      
      current = head.next();
      if (current != null) {
        head = current;
      } else {
        if (toBeCompleted != null) { 
          flow.dispatch(toBeCompleted);
        }
        return;
      }
    }
  }
}
