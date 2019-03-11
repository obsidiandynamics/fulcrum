package com.obsidiandynamics.flux;

import org.junit.*;

import com.obsidiandynamics.verifier.*;

public final class KryoTimestampedSerializerTest {
  @Test
  public void testRoundTrip() {
    final Timestamped<?> timestamped = new Timestamped<>("foo");
    RoundTripVerifier.forObject(timestamped).withCodec(new KryoCodec(new KryoTimestampedSerializer())).verify();
  }
}
