package com.obsidiandynamics.flux;

import org.junit.*;

import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.util.*;
import com.obsidiandynamics.verifier.*;

public final class OffHeapBackingQueueFactoryTest {
  @Test
  public void testPojo() {
    final Pool<Kryo> pool = new Pool<Kryo>(true, false) {
      @Override
      protected Kryo create() {
        final Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.setRegistrationRequired(false);
        return kryo;
      }
    };
    
    PojoVerifier.forClass(OffHeapBackingQueueFactory.class)
    .constructorArgs(new ConstructorArgs().with(Pool.class, pool))
    .verify();
  }
}
