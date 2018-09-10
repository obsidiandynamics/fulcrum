package com.obsidiandynamics.jgroups;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;
import java.util.UUID;
import java.util.concurrent.*;

import org.jgroups.*;
import org.jgroups.blocks.*;
import org.jgroups.blocks.ReplicatedHashMap.*;
import org.jgroups.util.*;
import org.junit.*;

import com.obsidiandynamics.await.*;

public final class ReplicatedHashMapTest {
  private static final ChannelFactory UDP_FACTORY = () -> Group.newUdpChannel(Util.getLocalhost());
  private static final ChannelFactory MOCK_FACTORY = () -> Group.newLoopbackChannel();
  
  private static final boolean MOCK = true;
  
  private static final ChannelFactory CHANNEL_FACTORY = MOCK ? MOCK_FACTORY : UDP_FACTORY;
  
  private final Set<Closeable> closeables = new HashSet<>();
  
  private final Timesert await = Timesert.wait(10_000);
  
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
  
  private <K, V> ReplicatedHashMap<K, V> createMap(ConcurrentMap<K,V> backingMap, JChannel channel) {
    final ReplicatedHashMap<K, V> replicatedHashMap = new ReplicatedHashMap<>(backingMap, channel);
    closeables.add(replicatedHashMap);
    return replicatedHashMap;
  }
  
  @Test
  public void testSimplePutAndGet() throws Exception {
    final String cluster = UUID.randomUUID().toString();
    
    final JChannel c0 = createChannel();
//    final JChannel c1 = createChannel();
    c0.connect(cluster);
//    c1.connect(cluster);
    
    final ConcurrentHashMap<String, String> backing = new ConcurrentHashMap<>();
    final ReplicatedHashMap<String, String> m0 = createMap(backing, c0);
    m0.addNotifier(new Notification<String, String>() {
      @Override
      public void entrySet(String key, String value) {
        System.out.format("entry set %s: %s\n", key, value);
      }

      @Override
      public void entryRemoved(String key) {
        // TODO Auto-generated method stub
        
      }

      @Override
      public void viewChange(View view, List<Address> mbrs_joined, List<Address> mbrs_left) {
        // TODO Auto-generated method stub
        
      }

      @Override
      public void contentsSet(Map<String, String> new_entries) {
        // TODO Auto-generated method stub
        
      }

      @Override
      public void contentsCleared() {
        // TODO Auto-generated method stub
        
      }
    });
    m0.setBlockingUpdates(true);
    m0.start(10_000);

//    final ReplicatedHashMap<String, String> m1 = createMap(c1);
//    m1.start(10_000);
    
    assertTrue(m0.isEmpty());
    
//    assertTrue(m1.isEmpty());
    
//    m0.put("key0", "value0");

    assertNull(m0.putIfAbsent("key0", "value0"));
    assertEquals("value0", m0.putIfAbsent("key0", "value1"));
    assertEquals("value0", m0.get("key0"));
    
//    await.until(() -> assertEquals(1, m0.size()));
//    await.until(() -> assertEquals(1, m1.size()));
//    assertEquals("value0", m0.get("key0"));
//    assertEquals("value0", m1.get("key0"));
//    
//    assertFalse(m1.replace("key0", "value1", "value0x"));
//    assertTrue(m1.replace("key0", "value0", "value0x"));
  }
}
