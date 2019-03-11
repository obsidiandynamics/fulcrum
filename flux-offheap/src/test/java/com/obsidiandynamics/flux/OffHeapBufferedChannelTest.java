package com.obsidiandynamics.flux;

import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.util.*;

public final class OffHeapBufferedChannelTest extends AbstractBufferedChannelTest {
  @Override
  protected BackingQueueFactory getBackingQueueFactory() {
    final Pool<Kryo> pool = new Pool<Kryo>(true, false) {
      @Override
      protected Kryo create() {
        final Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.setRegistrationRequired(false);
        return kryo;
      }
    };
    return new OffHeapBackingQueueFactory(pool);
  }
}
