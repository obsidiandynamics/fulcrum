package com.obsidiandynamics.flow;

import java.util.concurrent.atomic.*;

public final class StatefulConfirmation implements Confirmation {
  private final AtomicReference<StatefulConfirmation> next = new AtomicReference<>();
  
  private final Runnable task;
  
  private final AtomicInteger requested = new AtomicInteger();
  
  private final AtomicInteger completed = new AtomicInteger();
  
  private final Object id;
  
  private final FireController fireController;
  
  StatefulConfirmation(Object id, Runnable task, FireController fireController) {
    this.id = id;
    this.task = task;
    this.fireController = fireController;
  }
  
  @Override
  public void confirm() {
    completed.incrementAndGet();
    fireController.fire();
  }
  
  Object getId() {
    return id;
  }
  
  boolean isAnchor() {
    return task == null;
  }
  
  public boolean isConfirmed() {
    final int requested = this.requested.get();
    return requested != 0 && requested == completed.get();
  }
  
  void addRequest() {
    requested.incrementAndGet();
  }
  
  int getPendingCount() {
    return requested.get() - completed.get();
  }
  
  void appendTo(AtomicReference<StatefulConfirmation> tail) {
    final StatefulConfirmation t1 = tail.getAndSet(this);
    t1.next.lazySet(this);
  }
  
  Runnable getTask() {
    return task;
  }
  
  StatefulConfirmation next() {
    return next.get();
  }
  
  @Override
  public String toString() {
    return StatefulConfirmation.class.getSimpleName() + " [id=" + id + ", task=" + task + ", requested=" + requested + 
        ", completed=" + completed + ", next=" + next + "]";
  }

  static StatefulConfirmation anchor() {
    return new StatefulConfirmation(null, null, null);
  }
}
