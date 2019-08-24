package com.obsidiandynamics.flow;

import com.obsidiandynamics.worker.*;

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
