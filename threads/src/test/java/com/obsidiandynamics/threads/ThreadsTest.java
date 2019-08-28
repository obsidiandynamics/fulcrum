package com.obsidiandynamics.threads;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import com.obsidiandynamics.assertion.*;
import com.obsidiandynamics.func.*;
import com.obsidiandynamics.junit.*;
import com.obsidiandynamics.threads.Threads.*;

@RunWith(Parameterized.class)
public final class ThreadsTest {
  @Parameterized.Parameters
  public static List<Object[]> data() {
    return TestCycle.timesQuietly(1);
  }

  @After
  public void after() {
    assertFalse(Thread.interrupted());
  }

  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Threads.class);
  }

  @Test
  public void testDeferInterrupt() {
    assertTrue(Threads.deferInterrupt(() -> {}));
    assertFalse(Thread.interrupted());
  }

  @Test
  public void testDeferInterrupt_interrupted() {
    assertFalse(Threads.deferInterrupt(Exceptions.doThrow(InterruptedException::new)));
    assertTrue(Thread.interrupted());
  }

  @Test
  public void testDeferInterrupt_booleanSupplier() {
    assertTrue(Threads.deferInterrupt(() -> true, false));
    assertFalse(Threads.deferInterrupt(() -> false, true));
  }

  @Test
  public void testDeferInterrupt_booleanSupplier_withInterruptedException() {
    assertFalse(Threads.deferInterrupt(() -> {
      throw new InterruptedException();
    }, false));
    assertTrue(Thread.interrupted());
  }

  @Test
  public void testDeferInterrupt_supplier() {
    assertEquals("okay", Threads.deferInterrupt(() -> "okay", "interrupted"));
  }

  @Test
  public void testDeferInterrupt_supplier_withInterruptedException() {
    assertEquals("interrupted", Threads.deferInterrupt(() -> {
      throw new InterruptedException();
    }, "interrupted"));
    assertTrue(Thread.interrupted());
  }

  @Test
  public void testSleep_zeroAmount() {
    assertTrue(Threads.sleep(0));
    assertFalse(Thread.interrupted());
  }

  @Test
  public void testSleep_zeroAmount_interrupted() {
    Thread.currentThread().interrupt();
    assertFalse(Threads.sleep(0));
    assertTrue(Thread.interrupted());
  }

  @Test
  public void testSleep() {
    assertTrue(Threads.sleep(1));
    assertFalse(Thread.interrupted());
  }

  @Test
  public void testSleep_interrupted() {
    Thread.currentThread().interrupt();
    assertFalse(Threads.sleep(1));
    assertTrue(Thread.interrupted());
  }

  @Test
  public void testAwaitBarrier() {
    final CyclicBarrier barrier = new CyclicBarrier(2);
    new Thread(() -> {
      Threads.await(barrier);
    }).start();
    Threads.await(barrier);
  }

  @Test
  public void testAwaitBarrier_broken() throws InterruptedException {
    final CyclicBarrier barrier = new CyclicBarrier(2);
    final AtomicBoolean threadWasInterrupted = new AtomicBoolean();
    final AtomicReference<Throwable> errorRef = new AtomicReference<>();
    final Thread thread = new Thread(() -> {
      try {
        assertFalse(Threads.await(barrier));
        assertTrue(Thread.interrupted());
        threadWasInterrupted.set(true);
      } catch (Throwable e) {
        errorRef.set(e);
      }
    });
    thread.start();
    thread.interrupt(); // interrupting the thread breaks the barrier for the next thread trying to enter it

    assertNull(errorRef.get());    
    thread.join();
    assertTrue(threadWasInterrupted.get());

    assertThatThrownBy(() -> {
      Threads.await(barrier);
    }).isExactlyInstanceOf(RuntimeBrokenBarrierException.class).hasCauseExactlyInstanceOf(BrokenBarrierException.class);
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
  public void testAwaitLatch_interrupted() throws InterruptedException {
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
