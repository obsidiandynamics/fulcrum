package com.obsidiandynamics.flux;

import java.util.concurrent.*;

final class HeapBackingQueue<E> implements BackingQueue<E> {
  private final BlockingQueue<E> queue;
  
  HeapBackingQueue(int capacity) {
    queue = new LinkedBlockingQueue<>(capacity);
  }
  
  @Override
  public E poll(int timeoutMillis) throws InterruptedException {
    return queue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
  }

  @Override
  public void put(E element) throws InterruptedException {
    queue.put(element);
  }

  @Override
  public void dispose() {}
}
