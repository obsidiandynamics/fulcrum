package com.obsidiandynamics.jgroups;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class SyncPacketTest {
  @Test
  public void testToString() {
    Assertions.assertToStringOverride(Ack.forId(123));
  }
  
  @Test
  public void testEqualsHashCode() {
    final Ack a1 = Ack.forId(100);
    final Ack a2 = Ack.forId(200);
    final Ack a3 = Ack.forId(100);
    final Ack a4 = a1;

    assertNotEquals(a1, a2);
    assertEquals(a1, a3);
    assertEquals(a1, a4);
    assertNotEquals(a1, new Object());

    assertNotEquals(a1.hashCode(), a2.hashCode());
    assertEquals(a1.hashCode(), a3.hashCode());
  }
}
