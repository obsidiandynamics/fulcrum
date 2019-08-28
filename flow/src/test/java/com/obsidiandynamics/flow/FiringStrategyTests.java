package com.obsidiandynamics.flow;

import java.util.*;
import java.util.concurrent.atomic.*;

import com.obsidiandynamics.flow.FiringStrategy.*;
import com.obsidiandynamics.worker.*;

final class FiringStrategyTests {
  private FiringStrategyTests() {}
  
  static final class MockConfirmation implements Confirmation {
    private final Confirmation delegate;
    
    private final AtomicBoolean fired;
    
    MockConfirmation(Confirmation delegate, AtomicBoolean fired) {
      this.delegate = delegate;
      this.fired = fired;
    }

    @Override
    public void confirm() {
      delegate.confirm();
    }
    
    boolean isFired() {
      return fired.get();
    }
  }
  
  static final class MockFlow extends AbstractFlow {
    MockFlow(Factory firingStrategyFactory) {
      super(firingStrategyFactory);
    }

    @Override
    public Joinable terminate() {
      return this;
    }

    @Override
    public boolean join(long timeoutMillis) throws InterruptedException {
      return true;
    }

    @Override
    void fire() {}
    
    FiringStrategy getFiringStrategy() {
      return firingStrategy;
    }
    
    MockConfirmation begin() {
      final AtomicBoolean fired = new AtomicBoolean();
      final StatefulConfirmation delegate = begin(UUID.randomUUID(), () -> fired.set(true));
      return new MockConfirmation(delegate, fired);
    }
    
    MockConfirmation beginAndConfirm() {
      final MockConfirmation confirmation = begin();
      confirmation.confirm();
      return confirmation;
    }
  }
}
