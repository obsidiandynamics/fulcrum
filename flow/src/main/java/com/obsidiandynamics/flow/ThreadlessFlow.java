package com.obsidiandynamics.flow;

import com.obsidiandynamics.worker.*;

/**
 *  A {@link Flow} implementation that utilises the thread of the calling application
 *  to inspect the elements for completion and perform the necessary dispatch. <p>
 *  
 *  A {@link ThreadlessFlow} has two distinct advantages. Firstly, it doesn't spawn
 *  a thread, and can thus be instantiated in arbitrary numbers with minimal overhead.
 *  Secondly, because firing takes place on the calling thread (when the application
 *  invokes {@link StatefulConfirmation#confirm()}, the confirm-to-dispatch latency is
 *  minimal. <p>
 *  
 *  A drawback of this implementation is that it may hold up the calling application, particularly
 *  if the dispatch logic is inherently blocking or otherwise time-consuming. In these
 *  situations, the {@link ThreadedFlow} might be a more suitable alternative.
 *
 *  @see Flow
 */
public final class ThreadlessFlow extends AbstractFlow {
  public ThreadlessFlow(FiringStrategy.Factory firingStrategyFactory) {
    super(firingStrategyFactory);
  }
  
  @Override
  void fire() {
    synchronized (firingStrategy) {
      firingStrategy.fire();
    }
  }
  
  @Override
  public Joinable terminate() {
    return this;
  }
  
  @Override
  public boolean join(long timeoutMillis) {
    return true;
  }
}
