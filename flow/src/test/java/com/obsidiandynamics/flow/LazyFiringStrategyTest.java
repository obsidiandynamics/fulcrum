package com.obsidiandynamics.flow;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.flow.FiringStrategyTests.*;

public final class LazyFiringStrategyTest {
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
    
    assertTrue(flow.getPendingConfirmations().containsKey(c0.getId()));
    assertTrue(flow.getPendingConfirmations().containsKey(c1.getId()));
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
    
    assertTrue(flow.getPendingConfirmations().containsKey(c0.getId()));
    assertTrue(flow.getPendingConfirmations().containsKey(c1.getId()));
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
    
    assertFalse(flow.getPendingConfirmations().containsKey(c0.getId()));
    assertTrue(flow.getPendingConfirmations().containsKey(c1.getId()));
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
    
    assertFalse(flow.getPendingConfirmations().containsKey(c0.getId()));
    assertFalse(flow.getPendingConfirmations().containsKey(c1.getId()));
  }
}
