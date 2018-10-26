package com.obsidiandynamics.jgroups;

import static org.junit.Assert.*;

import java.util.*;

import org.jgroups.protocols.*;
import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class ProtocolsTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Protocols.class);
  }
  
  @Test
  public void testFindProtocolExisting() throws Exception {
    final Optional<SHARED_LOOPBACK> protocol = Protocols.findProtocol(Protocols.newLoopbackChannel().getProtocolStack(), 
                                                                      SHARED_LOOPBACK.class);
    assertTrue(protocol.isPresent());
  }
  
  @Test
  public void testFindProtocolNonExistent() throws Exception {
    final Optional<UDP> protocol = Protocols.findProtocol(Protocols.newLoopbackChannel().getProtocolStack(), 
                                                          UDP.class);
    assertFalse(protocol.isPresent());
  }
}
