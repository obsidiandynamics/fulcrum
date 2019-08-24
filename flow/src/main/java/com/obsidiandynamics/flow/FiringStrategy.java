package com.obsidiandynamics.flow;

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
