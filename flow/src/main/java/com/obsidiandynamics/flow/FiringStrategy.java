package com.obsidiandynamics.flow;

import java.util.concurrent.atomic.*;

public abstract class FiringStrategy {
  protected final ThreadedFlow flow;
  
  protected final AtomicReference<FlowConfirmation> tail;
  
  protected FlowConfirmation head;
  
  protected FlowConfirmation current;
  
  protected FiringStrategy(ThreadedFlow flow, AtomicReference<FlowConfirmation> tail) {
    this.flow = flow;
    this.tail = tail;
    head = tail.get();
    current = head;
  }
  
  abstract void fire();
  
  @FunctionalInterface
  public interface Factory {
    FiringStrategy create(ThreadedFlow flow, AtomicReference<FlowConfirmation> tail);
  }
}
