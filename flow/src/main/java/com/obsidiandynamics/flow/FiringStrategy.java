package com.obsidiandynamics.flow;

/**
 *  Influences dispatching behaviour with respect to contiguous sequences
 *  of completed {@link StatefulConfirmation}s in a {@link Flow} model. The application
 *  using a {@link Flow} has no control over the lifecycle of a {@link FiringStrategy}
 *  beyond specifying which type of strategy to instantiate by way of an
 *  appropriate {@link FiringStrategy.Factory}. The strategy instance is completely
 *  managed and fully contained within the encompassing {@link Flow}. <p>
 *  
 *  A firing strategy carries internal state is <em>not</em> thread-safe. The
 *  {@link Flow} implementation must ensure that access to a {@link FiringStrategy}
 *  instance is synchronized.
 *  
 *  @see StrictFiringStrategy
 *  @see LazyFiringStrategy
 *  @see Flow
 */
public abstract class FiringStrategy {
  protected final AbstractFlow flow;
  
  protected StatefulConfirmation head;
  
  protected StatefulConfirmation current;
  
  protected FiringStrategy(AbstractFlow flow, StatefulConfirmation head) {
    this.flow = flow;
    this.head = head;
    current = head;
  }
  
  abstract void fire();
  
  @FunctionalInterface
  public interface Factory {
    FiringStrategy create(AbstractFlow flow, StatefulConfirmation head);
  }
}
