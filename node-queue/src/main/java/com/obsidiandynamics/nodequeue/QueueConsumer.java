package com.obsidiandynamics.nodequeue;

import java.util.*;
import java.util.concurrent.atomic.*;

public final class QueueConsumer<E> implements Iterable<E> {
  private AtomicReference<LinkedNode<E>> head;
  
  QueueConsumer(AtomicReference<LinkedNode<E>> head) {
    this.head = head;
  }
  
  public E peek() {
    final LinkedNode<E> n = head.get();
    if (n != null) {
      return n.element;
    } else {
      return null;
    } 
  }
  
  public E poll() {
    final LinkedNode<E> n = head.get();
    if (n != null) {
      head = n;
      return n.element;
    } else {
      return null;
    }
  }
  
  public int drain(List<E> sink) {
    int drained = 0;
    for (E item; (item = poll()) != null; sink.add(item), drained++);
    return drained;
  }
  
  @Override
  public Iterator<E> iterator() {
    return new Iterator<E>() {
      private E next = poll();
      
      @Override
      public boolean hasNext() {
        return next != null;
      }

      @Override
      public E next() {
        try {
          return next;
        } finally {
          next = poll();
        }
      }
    };
  }
}