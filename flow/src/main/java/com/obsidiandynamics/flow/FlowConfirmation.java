package com.obsidiandynamics.flow;

import java.util.concurrent.atomic.*;

public final class FlowConfirmation implements Confirmation {
  private final AtomicReference<FlowConfirmation> next = new AtomicReference<>();
  
  private final Runnable task;
  
  private final AtomicInteger requested = new AtomicInteger();
  
  private final AtomicInteger completed = new AtomicInteger();
  
  private final Object id;
  
  FlowConfirmation(Object id, Runnable task) {
    this.id = id;
    this.task = task;
  }
  
  @Override
  public void confirm() {
    completed.incrementAndGet();
  }
  
  Object getId() {
    return id;
  }
  
  boolean isAnchor() {
    return task == null;
  }
  
  boolean isConfirmed() {
    final int requested = this.requested.get();
    return requested != 0 && requested == completed.get();
  }
  
  void addRequest() {
    requested.incrementAndGet();
  }
  
  int getPendingCount() {
    return requested.get() - completed.get();
  }
  
  void appendTo(AtomicReference<FlowConfirmation> tail) {
    final FlowConfirmation t1 = tail.getAndSet(this);
    t1.next.lazySet(this);
  }
  
  Runnable getTask() {
    return task;
  }
  
  FlowConfirmation next() {
    return next.get();
  }
  
  @Override
  public String toString() {
    return FlowConfirmation.class.getSimpleName() + " [task=" + task + ", requested=" + requested + 
        ", completed=" + completed + ", next=" + next + "]";
  }

  static FlowConfirmation anchor(Flow flow) {
    return new FlowConfirmation(flow, null);
  }
}
