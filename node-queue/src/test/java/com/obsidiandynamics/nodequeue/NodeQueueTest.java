package com.obsidiandynamics.nodequeue;

import static org.junit.Assert.*;

import java.util.*;
import java.util.stream.*;

import org.junit.*;

public final class NodeQueueTest {
  @Test
  public void testLateConsumer() {
    final int messages = 100;
    final NodeQueue<Long> q = new NodeQueue<>();
    LongStream.range(0, messages).forEach(q::add);
    final QueueConsumer<Long> consumer = q.consumer();
    assertNull(consumer.peek());
    final List<Long> consumed = consumeByDrain(consumer);
    assertEquals(0, consumed.size());
  }

  @Test
  public void testTwoEarlyConsumers() {
    final int messages = 100;
    final NodeQueue<Long> q = new NodeQueue<>();
    final QueueConsumer<Long> c0 = q.consumer();
    final QueueConsumer<Long> c1 = q.consumer();
    LongStream.range(0, messages).forEach(q::add);
    assertNotNull(c0.peek());
    assertNotNull(c1.peek());
    final List<Long> consumed0 = consumeByDrain(c0);
    final List<Long> consumed1 = consumeByIterator(c1);
    assertEquals(messages, consumed0.size());
    assertEquals(messages, consumed1.size());
  }
  
  private static List<Long> consumeByDrain(QueueConsumer<Long> consumer) {
    final List<Long> items = new ArrayList<>();
    final int drained = consumer.drain(items);
    assertEquals(drained, items.size());
    return items;
  }
  
  private static List<Long> consumeByIterator(QueueConsumer<Long> consumer) {
    final List<Long> items = new ArrayList<>();
    consumer.forEach(items::add);
    return items;
  }
}
