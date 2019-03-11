package com.obsidiandynamics.flux;

@FunctionalInterface
public interface BackingQueueFactory {
  <E> BackingQueue<E> create(int capacity);
}
