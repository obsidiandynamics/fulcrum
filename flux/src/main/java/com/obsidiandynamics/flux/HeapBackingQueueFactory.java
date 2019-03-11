package com.obsidiandynamics.flux;

public final class HeapBackingQueueFactory implements BackingQueueFactory {
  private static final HeapBackingQueueFactory INSTANCE = new HeapBackingQueueFactory();
  
  public static HeapBackingQueueFactory getInstance() { return INSTANCE; }
  
  private HeapBackingQueueFactory() {}
  
  @Override
  public <K> BackingQueue<K> create(int capacity) {
    return new HeapBackingQueue<>(capacity);
  }
}
