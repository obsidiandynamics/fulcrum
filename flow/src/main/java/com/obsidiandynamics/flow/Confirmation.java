package com.obsidiandynamics.flow;

/**
 *  A primitive accompanying handle to an element in an ordered {@link Flow} sequence that may be
 *  processed in arbitrary order, but must be finalised in strict sequence order.
 *  
 *  @see Flow
 *  @see StatefulConfirmation
 */
@FunctionalInterface
public interface Confirmation {
  void confirm();
}
