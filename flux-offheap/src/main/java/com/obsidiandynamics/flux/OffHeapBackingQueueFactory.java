package com.obsidiandynamics.flux;

import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.util.*;

public final class OffHeapBackingQueueFactory implements BackingQueueFactory {
  private final Pool<Kryo> pool;
  
  public Pool<Kryo> getPool() {
    return pool;
  }

  public OffHeapBackingQueueFactory(Pool<Kryo> pool) {
    this.pool = pool;
  }

  @Override
  public <E> BackingQueue<E> create(int capacity) {
    return new OffHeapBackingQueue<>(pool, capacity);
  }

  @Override
  public String toString() {
    return OffHeapBackingQueueFactory.class.getSimpleName() + " [pool=" + pool + "]";
  }
}
