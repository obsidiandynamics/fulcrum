package com.obsidiandynamics.jgroups;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

import nl.jqno.equalsverifier.*;

public final class SyncPacketTest {
  @Test
  public void testToString() {
    Assertions.assertToStringOverride(Ack.forId(123));
  }
  
  @Test
  public void testEqualsHashCode() {
    EqualsVerifier.forClass(SyncPacket.class).verify();
  }
}
