package com.obsidiandynamics.flow;

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
