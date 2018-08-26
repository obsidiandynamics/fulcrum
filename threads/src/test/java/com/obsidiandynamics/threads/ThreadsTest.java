package com.obsidiandynamics.threads;

import static org.junit.Assert.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;
import com.obsidiandynamics.func.*;
import com.obsidiandynamics.threads.Threads.*;

public final class ThreadsTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Threads.class);
  }

  @Test
  public void testRunUninterruptedlyNormal() {
    assertTrue(Threads.runUninterruptedly(() -> {}));
    assertFalse(Thread.interrupted());
  }

  @Test
  public void testRunUninterruptedlyInterrupted() {
    try {
      assertFalse(Threads.runUninterruptedly(Exceptions.doThrow(InterruptedException::new)));
      assertTrue(Thread.interrupted());
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  public void testNoSleepUninterrupted() {
    assertTrue(Threads.sleep(0));
    assertFalse(Thread.interrupted());
  }

  @Test
  public void testNoSleepInterrupted() {
    try {
      Thread.currentThread().interrupt();
      assertFalse(Threads.sleep(0));
      assertTrue(Thread.interrupted());
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  public void testSleepUninterrupted() {
    assertTrue(Threads.sleep(1));
    assertFalse(Thread.interrupted());
  }

  @Test
  public void testSleepInterrupted() {
    try {
      Thread.currentThread().interrupt();
      assertFalse(Threads.sleep(1));
      assertTrue(Thread.interrupted());
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  public void testAwaitBarrier() {
    final CyclicBarrier barrier = new CyclicBarrier(2);
    new Thread(() -> {
      Threads.await(barrier);
    }).start();
    Threads.await(barrier);
  }

  @Test(expected=RuntimeBrokenBarrierException.class)
  public void testAwaitBarrierBroken() throws InterruptedException {
    final CyclicBarrier barrier = new CyclicBarrier(2);
    final AtomicBoolean threadWasInterrupted = new AtomicBoolean();
    final Thread thread = new Thread(() -> {
      Threads.await(barrier);
      assertTrue(Thread.interrupted());
      threadWasInterrupted.set(true);;
    });
    thread.start();
    thread.interrupt(); // interrupting the thread breaks the barrier
    Threads.await(barrier);
    
    thread.join();
    assertTrue(threadWasInterrupted.get());
  }

  @Test
  public void testAwaitLatch() {
    final CountDownLatch latch = new CountDownLatch(1);
    new Thread(() -> {
      latch.countDown();
    }).start();
    Threads.await(latch);
  }

  @Test
  public void testAwaitLatchInterrupted() throws InterruptedException {
    final AtomicBoolean threadWasInterrupted = new AtomicBoolean();
    final CountDownLatch latch = new CountDownLatch(1);
    final Thread thread = new Thread(() -> {
      Threads.await(latch);
      assertTrue(Thread.interrupted());
      threadWasInterrupted.set(true);;
    });
    thread.start();
    thread.interrupt();
    
    thread.join();
    assertTrue(threadWasInterrupted.get());
  }

  @Test
  public void testTookNanos() {
    final long took = Threads.tookNanos(() -> Threads.sleep(5));
    assertTrue("took=" + took, took >= 5_000_000L);
    assertTrue("took=" + took, took < 1_000_000_000L);
  }

  @Test
  public void testTookMillis() {
    final long took = Threads.tookMillis(() -> Threads.sleep(5));
    assertTrue("took=" + took, took >= 5);
    assertTrue("took=" + took, took < 1_000);
  }
}
