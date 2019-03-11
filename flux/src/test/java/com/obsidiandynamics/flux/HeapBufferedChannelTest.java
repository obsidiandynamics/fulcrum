package com.obsidiandynamics.flux;

public final class HeapBufferedChannelTest extends AbstractBufferedChannelTest {
  @Override
  protected BackingQueueFactory getBackingQueueFactory() {
    return HeapBackingQueueFactory.getInstance();
  }
}
