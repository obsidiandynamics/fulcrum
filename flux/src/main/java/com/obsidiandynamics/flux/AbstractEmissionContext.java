package com.obsidiandynamics.flux;

import static com.obsidiandynamics.func.Functions.*;

import java.util.*;
import java.util.function.*;

public abstract class AbstractEmissionContext<E> implements EmissionContext<E>, Iterator<E> {
  private boolean contextTerminated;
  
  private static final class Node<E> {
    E event;
    Node<E> next;
    
    Node(E event, Node<E> previous) {
      this.event = event;
      if (previous != null) {
        previous.next = this;
      }
    }
    
    E clearEvent() {
      final E event = this.event;
      this.event = null;
      return event;
    }
  }
  
  private Node<E> head;
  
  private Node<E> tail;
  
  private int size;
  
  private int limit = Integer.MAX_VALUE;
  
  {
    tail = head = new Node<>(null, null);
  }
  
  @Override
  public final void terminate() {
    mustBeFalse(hasNext(), illegalState("At least one event has already been emitted"));
    ensureNotTerminated();
    contextTerminated = true;
    terminateImpl();
  }
  
  protected abstract void terminateImpl();

  @Override
  public final void emit(E event) {
    ensureNotTerminated();
    mustExist(event, "Event cannot be null");
    ensureRemainingCapacity();
    
    if (size++ == 0) {
      head.event = event;
    } else {
      tail = new Node<>(event, tail);
    }
  }
  
  @Override
  public final int remainingCapacity() {
    return limit - size;
  }
  
  public final int getLimit() {
    return limit;
  }
  
  public final void setLimit(int limit) {
    mustBeGreaterOrEqual(limit, 0, illegalArgument("Limit must be greater or equal to 0"));
    this.limit = limit;
  }
  
  public final void decrementLimit() {
    mustBeGreater(limit, 0, illegalArgument("No remaining capacity"));
    limit--;
  }
  
  public final boolean isTerminated() {
    return contextTerminated;
  }
  
  public final int size() {
    return size;
  }
  
  @Override
  public final boolean hasNext() {
    return size > 0;
  }
  
  @Override
  public final E next() {
    if (size > 1) {
      size--;
      final Node<E> oldHead = head;
      head = head.next;
      return oldHead.event;
    } else if (size == 1) {
      size--;
      return head.clearEvent();
    } else {
      throw new NoSuchElementException("No more events");
    }
  }

  private void ensureNotTerminated() {
    mustBeFalse(contextTerminated, illegalState("Context has been terminated"));
  }
  
  private void ensureRemainingCapacity() {
    mustBeLess(size, limit, illegalState("No remaining capacity"));
  }
  
  public List<E> getEventsCopy() {
    final List<E> events = new ArrayList<>(size);
    copyEvents(events::add);
    return events;
  }
  
  public void copyEvents(Consumer<? super E> target) {
    for (Node<E> current = this.head; current != null && current.event != null; current = current.next) {
      target.accept(current.event);
    }
  }
  
  @Override
  public String toString() {
    return AbstractEmissionContext.class.getSimpleName() + " [events=" + getEventsCopy() + "]";
  }
}
