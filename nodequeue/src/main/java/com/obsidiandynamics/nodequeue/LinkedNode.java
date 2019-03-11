package com.obsidiandynamics.nodequeue;

import java.util.concurrent.atomic.*;

public final class LinkedNode<E> extends AtomicReference<LinkedNode<E>> {
  private static final long serialVersionUID = 1L;

  public final E element;

  public LinkedNode(E element) { this.element = element; }
  
  public static <T> LinkedNode<T> anchor() {
    return new LinkedNode<>(null);
  }
  
  public void appendTo(AtomicReference<LinkedNode<E>> tail) {
    final LinkedNode<E> t1 = tail.getAndSet(this);
    t1.lazySet(this);
  }
}