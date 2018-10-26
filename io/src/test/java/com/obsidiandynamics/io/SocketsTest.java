package com.obsidiandynamics.io;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.io.*;
import java.net.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;
import com.obsidiandynamics.io.Sockets.*;

public final class SocketsTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Sockets.class);
  }
  
  @Test
  public void testRandomInRange() {
    final int cycles = 10;
    for (int i = 0; i < cycles; i++) {
      final int random = Sockets.randomInRange(10, 14);
      assertThat(random).isBetween(10, 13);
    }
  }
  
  @Test
  public void testRandomInRangeSingle() {
    assertEquals(10, Sockets.randomInRange(10, 11));
  }
  
  @Test
  public void testNextInRange() {
    assertEquals(11, Sockets.nextInRange(10, 10, 13));
    assertEquals(12, Sockets.nextInRange(11, 10, 13));
    assertEquals(10, Sockets.nextInRange(12, 10, 13));
  }
  
  @Test
  public void testGetSparePortIllegalArgument() {
    assertThatThrownBy(() -> Sockets.getSparePort(1023, 4000))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("Lower port range must include 1024 or higher");
    

    assertThatThrownBy(() -> Sockets.getSparePort(4000, 65537))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("Upper port range must exclude 65536 or lower");
    

    assertThatThrownBy(() -> Sockets.getSparePort(4000, 4000))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("Port range cannot overlap");
  }
  
  /**
   *  Tests maximum allowable range.
   *  
   *  @throws PortRangeExhaustedException
   */
  @Test
  public void testGetSparePortMaximumRangeSuccess() throws PortRangeExhaustedException {
    final int spare = Sockets.getSparePort(1024, 65536);
    assertThat(spare).isBetween(1024, 65535);
  }
  
  /**
   *  Tests a range comprising just one port, with the port being acquired beforehand, thus
   *  forcing an exhaustion.
   *  
   *  @throws PortRangeExhaustedException
   *  @throws IOException
   */
  @Test(expected=PortRangeExhaustedException.class)
  public void testGetSparePortRangeExchausted() throws PortRangeExhaustedException, IOException {
    final int spare = Sockets.getSparePort(2000, 3000);
    try (ServerSocket socket = new ServerSocket()) {
      socket.setReuseAddress(true);
      socket.bind(new InetSocketAddress(spare));
      
      Sockets.getSparePort(spare, spare + 1);
    }
  }
  
  /**
   *  Tests a short range, comprising just two ports, where the first port in the range is busy and
   *  the second port is available. <p>
   *  
   *  Due to randomness in picking the initial port in the range, the {@link Sockets#getSparePort(int, int)}
   *  method has a 50% chance of hitting the available port on the first attempt, which would mean that the 
   *  looping logic wouldn't be exercise. <p>
   *  
   *  The test is repeated N times, meaning that the likelihood of never exercising the looping
   *  logic is 1/(2^N). With N=20, the probability of achieving full coverage is 0.9999990463.
   *  
   *  @throws PortRangeExhaustedException
   *  @throws IOException
   */
  @Test
  public void testGetSparePortInShortRangeWithSomeInUse() throws PortRangeExhaustedException, IOException {
    final int cycles = 20;
    for (int cycle = 0; cycle < cycles; cycle++) {
      for (;;) {
        final int spare = Sockets.getSparePort(2000, 3000);
        final boolean nextAvailable = Sockets.isLocalPortAvailable(spare + 1);
        if (! nextAvailable) continue;
        
        try (ServerSocket socket = new ServerSocket()) {
          socket.setReuseAddress(true);
          socket.bind(new InetSocketAddress(spare));
          
          Sockets.getSparePort(spare, spare + 2);
        }
        break;
      }
    }
  }
  
  @Test
  public void testIsRemotePortListening() throws PortRangeExhaustedException, IOException {
    final int spare = Sockets.getSparePort(2000, 3000);
    assertFalse(Sockets.isRemotePortListening("localhost", spare, 10_000));
    
    try (ServerSocket socket = new ServerSocket()) {
      socket.setReuseAddress(true);
      socket.bind(new InetSocketAddress(spare));
      
      assertTrue(Sockets.isRemotePortListening("localhost", spare, 10_000));
    }
  }
}
