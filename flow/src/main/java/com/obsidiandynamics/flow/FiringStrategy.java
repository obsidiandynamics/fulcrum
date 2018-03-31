package com.obsidiandynamics.flow;

import java.util.concurrent.atomic.*;

import com.obsidiandynamics.worker.*;

public abstract class FiringStrategy implements WorkerCycle {
  protected static final int CYCLE_IDLE_INTERVAL_MILLIS = 1;
  
  protected final Flow flow;
  
  protected final AtomicReference<FlowConfirmation> tail;
  
  protected FlowConfirmation head;
  
  protected FlowConfirmation current;
  
  protected FiringStrategy(Flow flow, AtomicReference<FlowConfirmation> tail) {
    this.flow = flow;
    this.tail = tail;
    head = tail.get();
    current = head;
  }
  
  @FunctionalInterface
  public interface Factory {
    FiringStrategy create(Flow flow, AtomicReference<FlowConfirmation> tail);
  }
}
