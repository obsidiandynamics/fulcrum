package com.obsidiandynamics.flow;

/**
 *  Dispatches every element in a contiguous sequence of completed {@link StatefulConfirmation}s
 *  within in a {@link Flow} model, additionally
 *  ensuring that the preceding sequences are dispatched before their successors. <p>
 *  
 *  A {@link StrictFiringStrategy} is the strictest of {@link FiringStrategy} implementations, 
 *  and provides additional guarantees over a {@link LazyFiringStrategy}. In other words, it 
 *  is suitable in all situations where a {@link LazyFiringStrategy} may be used.
 *  
 *  @see FiringStrategy
 *  @see Flow
 */
public final class StrictFiringStrategy extends FiringStrategy {
  public StrictFiringStrategy(AbstractFlow flow, StatefulConfirmation head) {
    super(flow, head);
  }

  @Override
  void fire() {
    for (;;) {
      if (current != null) {
        if (current.isAnchor()) {
          // skip the anchor
        } else if (current.isConfirmed()) {
          flow.dispatch(current);
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
