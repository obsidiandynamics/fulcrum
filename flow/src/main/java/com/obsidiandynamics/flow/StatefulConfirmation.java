package com.obsidiandynamics.flow;

import java.util.concurrent.atomic.*;

/**
 *  A {@link Confirmation} that tracks the number of initiating requests to the
 *  encompassing {@link Flow}, requiring an equivalent number of calls to 
 *  {@link #confirm()} before being deemed as complete.
 *
 *  @see Confirmation
 *  @see Flow
 */
public final class StatefulConfirmation implements Confirmation {
  private final AtomicReference<StatefulConfirmation> next = new AtomicReference<>();
  
  private final Runnable onComplete;
  
  private final AtomicInteger requested = new AtomicInteger();
  
  private final AtomicInteger completed = new AtomicInteger();
  
  private final Object id;
  
  private final FireController fireController;
  
  StatefulConfirmation(Object id, Runnable onComplete, FireController fireController) {
    this.id = id;
    this.onComplete = onComplete;
    this.fireController = fireController;
  }
  
  @Override
  public void confirm() {
    final int numCompleted = completed.incrementAndGet();
    final int numRequested = requested.get();
    if (numCompleted > numRequested) {
      completed.decrementAndGet();
      throw new IllegalStateException("Completed " + numCompleted + " of " + numRequested + " for ID " + id);
    }
    fireController.fire();
  }
  
  public Object getId() {
    return id;
  }
  
  boolean isAnchor() {
    return onComplete == null;
  }
  
  public boolean isConfirmed() {
    final int requested = this.requested.get();
    return requested != 0 && completed.get() >= requested;
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
    return onComplete;
  }
  
  StatefulConfirmation next() {
    return next.get();
  }
  
  @Override
  public String toString() {
    return StatefulConfirmation.class.getSimpleName() + " [id=" + id + ", onComplete=" + onComplete + ", requested=" + requested + 
        ", completed=" + completed + "]";
  }

  static StatefulConfirmation anchor() {
    return new StatefulConfirmation(null, null, null);
  }
}
