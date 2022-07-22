package com.obsidiandynamics.flux;

import static org.junit.Assert.*;

import java.lang.Thread.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.assertj.core.api.*;
import org.junit.*;

import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.util.*;
import com.obsidiandynamics.await.*;
import com.obsidiandynamics.threads.*;

public final class OffHeapBackingQueueTest {
  private static final boolean DEBUG = true;
  
  private static final int SCALE = 1;
  
  private static Pool<Kryo> pool;

  @BeforeClass
  public static void beforeClass() {
    pool = new Pool<Kryo>(true, false) {
      @Override
      protected Kryo create() {
        final Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.setRegistrationRequired(false);
        return kryo;
      }
    };
  }

  @AfterClass
  public static void afterClass() {
    pool = null;
  }

  private final List<OffHeapBackingQueue<?>> queues = new ArrayList<>();

  @After
  public void after() {
    queues.forEach(BackingQueue::dispose);
  }
  
  private OffHeapBackingQueue<String> newQueue(int capacity) {
    final OffHeapBackingQueue<String> queue = new OffHeapBackingQueue<>(pool, capacity);
    queues.add(queue);
    return queue;
  }
  
  @Test
  public void testPoll_empty() throws InterruptedException {
    final OffHeapBackingQueue<String> queue = newQueue(1);
    assertNull(queue.poll(1));
  }
  
  @Test
  public void testPoll_empty_interrupted() {
    final OffHeapBackingQueue<String> queue = newQueue(1);
    Thread.currentThread().interrupt();
    Assertions.assertThatThrownBy(() -> {
      queue.poll(Integer.MAX_VALUE);
    }).isInstanceOf(InterruptedException.class);
    assertFalse(Thread.interrupted());
  }
  
  @Test
  public void testPoll_nonEmpty_interrupted() throws InterruptedException {
    final OffHeapBackingQueue<String> queue = newQueue(1);
    queue.put("zero");
    Thread.currentThread().interrupt();
    Assertions.assertThatThrownBy(() -> {
      queue.poll(Integer.MAX_VALUE);
    }).isInstanceOf(InterruptedException.class);
    assertFalse(Thread.interrupted());
  }
  
  @Test
  public void testPutPoll_withinCapacity() throws InterruptedException {
    final List<String> elements = Arrays.asList("zero", "one", "two");
    final OffHeapBackingQueue<String> queue = newQueue(elements.size());
    for (String element : elements) {
      queue.put(element);
    }
    
    final List<String> pulled = new ArrayList<>(elements.size());
    for (;;) {
      final String element = queue.poll(10);
      if (element != null) {
        pulled.add(element);
      } else {
        break;
      }
    }
    assertEquals(elements, pulled);
  }
  
  @Test
  public void testPut_full() throws Throwable {
    final OffHeapBackingQueue<String> queue = newQueue(1);
    queue.put("zero");
    
    final AtomicReference<Throwable> errorRef = new AtomicReference<>();
    final Thread t = new Thread(() -> {
      try {
        queue.put("one");
        fail("Expected interrupt");
      } catch (InterruptedException e) {
        // expected
      } catch (Throwable e) {
        e.printStackTrace();
        errorRef.set(e);
      }
    });
    t.start();
    
    Timesert.wait(30_000).untilTrue(() -> t.getState() == State.TIMED_WAITING);
    t.interrupt();
    t.join();
    
    if (errorRef.get() != null) throw errorRef.get();
    
    assertEquals("zero", queue.poll(10));
    queue.put("one");
  }
  
  @Test
  public void testPutPoll_disposed_withinCapacity() throws InterruptedException {
    final OffHeapBackingQueue<String> queue = newQueue(1);
    queue.dispose();
    queue.put("zero");
    
    assertNull(queue.poll(Integer.MAX_VALUE));
  }
  
  @Test
  public void testPutPoll_disposed_overCapacity() throws InterruptedException {
    final OffHeapBackingQueue<String> queue = newQueue(1);
    queue.put("zero");
    
    queue.dispose();
    queue.put("one");
    
    assertNull(queue.poll(Integer.MAX_VALUE));
  }
  
  @Test
  public void testPutPoll_bounded_slowConsumer() throws Throwable {
    testPutPoll_separateConsumerThread(5, 10, 10);
  }
  
  @Test
  public void testPutPoll_bounded_fastConsumer() throws Throwable {
    final int capacity = 10 * SCALE;
    final int elements = 1_000 * SCALE;
    
    final long took = Threads.tookMillis(() -> testPutPoll_separateConsumerThread(capacity, elements, 0));
    final double rate = elements * 1000d / took;
    if (DEBUG) System.out.format("bounded_fastConsumer(): took %,d ms, %,.0f msg/s\n", took, rate);
  }
  
  @Test
  public void testPutPoll_unbounded_fastConsumer() throws Throwable {
    final int capacity = Integer.MAX_VALUE;
    final int elements = 1_000 * SCALE;
    
    final long took = Threads.tookMillis(() -> testPutPoll_separateConsumerThread(capacity, elements, 0));
    final double rate = elements * 1000d / took;
    if (DEBUG) System.out.format("unbounded_fastConsumer(): took %,d ms, %,.0f msg/s\n", took, rate);
  }

  private void testPutPoll_separateConsumerThread(int capacity, int elements, int pollSleepMillis) throws Throwable {
    final OffHeapBackingQueue<String> queue = newQueue(capacity);
    
    final AtomicReference<Throwable> errorRef = new AtomicReference<>();
    final Thread t = new Thread(() -> {
      try {
        for (int i = 0; i < elements; i++) {
          final String polled = queue.poll(Integer.MAX_VALUE);
          assertNotNull(polled);
          final int polledInt = Integer.parseInt(polled);
          assertEquals(i, polledInt);
          Threads.sleep(pollSleepMillis);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        errorRef.set(e);
      }
    }, "poller");
    t.start();
    
    for (int i = 0; i < elements; i++) {
      queue.put(String.valueOf(i));
    }
    
    t.join();
    if (errorRef.get() != null) throw errorRef.get();
  }
  
  @Test
  public void testPutPoll_echoTwoQueues() throws Throwable {
    final int elements = 100 * SCALE;
    
    final OffHeapBackingQueue<String> requests = newQueue(1);
    final OffHeapBackingQueue<String> replies = newQueue(1);
    
    final AtomicReference<Throwable> errorRef = new AtomicReference<>();
    final Thread t = new Thread(() -> {
      try {
        for (int i = 0; i < elements; i++) {
          final String polled = requests.poll(Integer.MAX_VALUE);
          assertNotNull(polled);
          final int polledInt = Integer.parseInt(polled);
          assertEquals(i, polledInt);
          replies.put(polled);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        errorRef.set(e);
      }
    }, "echo");
    t.start();
    
    final long took = Threads.tookMillis(() -> {
      for (int i = 0; i < elements; i++) {
        final String request = String.valueOf(i);
        requests.put(request);
        final String reply = replies.poll(Integer.MAX_VALUE);
        assertEquals(request, reply);
      }
    });
    
    final double rate = elements * 1000d / took;
    final double avgRoundTripTimeMicros = 1_000_000d / rate;
    if (DEBUG) System.out.format("testPutPoll_echoTwoQueues(): took %,d ms, %,.3f µs/pair (avg round-trip), %,.0f msg/s\n", 
                                 took, avgRoundTripTimeMicros ,rate);

    t.join();
    if (errorRef.get() != null) throw errorRef.get();
  }
}
