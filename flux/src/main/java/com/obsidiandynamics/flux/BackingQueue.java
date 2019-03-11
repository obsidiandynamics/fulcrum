package com.obsidiandynamics.flux;

public interface BackingQueue<E> {
  E poll(int timeoutMillis) throws InterruptedException;
  
  void put(E element) throws InterruptedException;
  
  void dispose();
}
