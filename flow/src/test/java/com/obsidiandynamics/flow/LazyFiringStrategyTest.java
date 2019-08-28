package com.obsidiandynamics.flow;

import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.junit.*;

import com.obsidiandynamics.flow.FiringStrategy.*;
import com.obsidiandynamics.worker.*;

public final class LazyFiringStrategyTest {
  private static final class MockConfirmation implements Confirmation {
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
  
  private static final class MockFlow extends AbstractFlow {
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
  
  private MockFlow flow;
  
  @Before
  public void before() {
    flow = new MockFlow(LazyFiringStrategy::new);
  }
  
  @Test
  public void testFire_empty() {
    flow.getFiringStrategy().fire();
    flow.getFiringStrategy().fire();
  }
  
  @Test
  public void testFire_neitherConfirmed() {
    final MockConfirmation c0 = flow.begin();
    final MockConfirmation c1 = flow.begin();
    
    flow.getFiringStrategy().fire();
    assertFalse(c0.isFired());
    assertFalse(c1.isFired());
    
    flow.getFiringStrategy().fire();
    assertFalse(c0.isFired());
    assertFalse(c1.isFired());
  }
  
  @Test
  public void testFire_firstNotConfirmedSecondConfirmed() {
    final MockConfirmation c0 = flow.begin();
    final MockConfirmation c1 = flow.beginAndConfirm();
    
    flow.getFiringStrategy().fire();
    assertFalse(c0.isFired());
    assertFalse(c1.isFired());
    
    flow.getFiringStrategy().fire();
    assertFalse(c0.isFired());
    assertFalse(c1.isFired());
  }
  
  @Test
  public void testFire_firstConfirmedSecondNotConfirmed() {
    final MockConfirmation c0 = flow.beginAndConfirm();
    final MockConfirmation c1 = flow.begin();
    
    flow.getFiringStrategy().fire();
    assertTrue(c0.isFired());
    assertFalse(c1.isFired());
    
    flow.getFiringStrategy().fire();
    assertTrue(c0.isFired());
    assertFalse(c1.isFired());
  }
  
  @Test
  public void testFire_bothConfirmed() {
    final MockConfirmation c0 = flow.beginAndConfirm();
    final MockConfirmation c1 = flow.beginAndConfirm();
    
    flow.getFiringStrategy().fire();
    assertFalse(c0.isFired());
    assertTrue(c1.isFired());
    
    flow.getFiringStrategy().fire();
    assertFalse(c0.isFired());
    assertTrue(c1.isFired());
  }
}
