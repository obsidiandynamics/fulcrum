package com.obsidiandynamics.jgroups;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;
import java.util.UUID;
import java.util.concurrent.*;

import org.jgroups.*;
import org.jgroups.blocks.*;
import org.jgroups.util.*;
import org.junit.*;

public final class ReplicatedHashMapTest {
  private static final ChannelFactory UDP_FACTORY = () -> Protocols.newUdpChannel(Util.getLoopback());
  private static final ChannelFactory MOCK_FACTORY = () -> Protocols.newLoopbackChannel();
  
  private static final boolean MOCK = true;
  
  private static final ChannelFactory CHANNEL_FACTORY = MOCK ? MOCK_FACTORY : UDP_FACTORY;
  
  private final Set<Closeable> closeables = new HashSet<>();
  
  @After
  public void after() throws IOException {
    for (Closeable closeable : closeables) {
      closeable.close();
    }
    closeables.clear();
  }
  
  private JChannel createChannel() throws Exception {
    final JChannel channel = CHANNEL_FACTORY.create();
    closeables.add(channel);
    return channel;
  }
  
  private <K, V> ReplicatedHashMap<K, V> createMap(JChannel channel) {
    return createMap(new ConcurrentHashMap<>(), channel);
  }
  
  private <K, V> ReplicatedHashMap<K, V> createMap(ConcurrentMap<K,V> backingMap, JChannel channel) {
    final ReplicatedHashMap<K, V> replicatedHashMap = new ReplicatedHashMap<>(backingMap, channel);
    closeables.add(replicatedHashMap);
    return replicatedHashMap;
  }
  
  @Test
  public void testSimplePutAndGet() throws Exception {
    final String cluster = UUID.randomUUID().toString();
    
    final JChannel c0 = createChannel();
    final JChannel c1 = createChannel();
    c0.connect(cluster);
    c1.connect(cluster);
    
    final ReplicatedHashMap<String, String> m0 = createMap(c0);
    m0.setBlockingUpdates(true);
    m0.start(10_000);

    final ReplicatedHashMap<String, String> m1 = createMap(c1);
    m1.setBlockingUpdates(true);
    m1.start(10_000);
    
    assertTrue(m0.isEmpty());
    assertTrue(m1.isEmpty());
    
    assertNull(m0.putIfAbsent("key0", "value0"));
    assertEquals("value0", m1.putIfAbsent("key0", "value1"));
    assertEquals("value0", m1.get("key0"));
  }
}
