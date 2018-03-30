package com.obsidiandynamics.nodequeue;

import java.util.concurrent.atomic.*;

/**
 *  A generic, high-performance, lock-free, unbounded MPMC (multi-producer, multi-consumer) queue
 *  implementation, adapted from Indigo's scheduler.<p>
 *  
 *  @see <a href="https://github.com/obsidiandynamics/indigo/blob/4b13815d1aefb0e5a5a45ad89444ced9f6584e20/src/main/java/com/obsidiandynamics/indigo/NodeQueueActivation.java">NodeQueueActivation</a>
 *  
 *  @param <E> Element type.
 */
public final class NodeQueue<E> {
  private final AtomicReference<LinkedNode<E>> tail = new AtomicReference<>(LinkedNode.anchor());
  
  public void add(E item) {
    new LinkedNode<>(item).appendTo(tail);
  }
  
  public QueueConsumer<E> consumer() {
    return new QueueConsumer<>(tail.get());
  }
}
