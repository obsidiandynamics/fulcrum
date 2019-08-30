package com.obsidiandynamics.threads;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import com.obsidiandynamics.junit.*;

@RunWith(Parameterized.class)
public final class ReentrantAffiliateLockTest {
  @Parameterized.Parameters
  public static List<Object[]> data() {
    return TestCycle.timesQuietly(1);
  }
  
  @Test
  public void testUncontended() throws InterruptedException {
    final int runs = 100;
    final ReentrantAffiliateLock lock = new ReentrantAffiliateLock();
    final int timeout = 10_000;

    for (int i = 0; i < runs; i++) {
      assertTrue(lock.tryLock(timeout, Thread.currentThread()));
      assertTrue(lock.tryLock(timeout, Thread.currentThread())); // second acquisition should be reentrant
      lock.unlock(Thread.currentThread());
      lock.unlock(Thread.currentThread());
    }
  }
  
  private static final class UnsynchronizedInt {
    int value;
  }

  @Test
  public void testContended() throws InterruptedException {
    final int threads = 5;
    final int runsPerThread = 10;
    final int holdTime = 1;
    final String id = "id";
    final int timeout = Integer.MAX_VALUE;

    final ReentrantAffiliateLock lock = new ReentrantAffiliateLock();
    final List<Throwable> errors = new CopyOnWriteArrayList<>();
    final CountDownLatch completedLatch = new CountDownLatch(threads);
    final AtomicInteger acquisitionCount = new AtomicInteger();

    final UnsynchronizedInt unsynchronizedInt = new UnsynchronizedInt();

    for (int t = 0; t < threads; t++) {
      final String _t = String.valueOf(t);
      final Thread thread = new Thread(() -> {
        try {
          for (int r = 0; r < runsPerThread; r++) {
            assertTrue(lock.tryLock(timeout, _t));
            assertTrue(lock.tryLock(timeout, _t)); // second acquisition should be reentrant
            final int newAcquisitionCount = acquisitionCount.incrementAndGet();
            assertEquals(1, newAcquisitionCount);
            final int unsynchronizedIntValueBeforeSleep = ++unsynchronizedInt.value;

            Threads.sleep(holdTime);

            assertEquals(unsynchronizedIntValueBeforeSleep, unsynchronizedInt.value);
            acquisitionCount.decrementAndGet();
            lock.unlock(_t);
            lock.unlock(_t);
          }
        } catch (Throwable e) {
          e.printStackTrace();
          errors.add(e);
        } finally {
          completedLatch.countDown();
        }
      }, id + "-" + t);
      thread.start();
    }

    completedLatch.await(60, TimeUnit.SECONDS);
    assertThat(errors).isEmpty();
    assertEquals(threads * runsPerThread, unsynchronizedInt.value);
  }

  @Test
  public void testTryLock_withTimeout() throws InterruptedException {
    final ReentrantAffiliateLock lock = new ReentrantAffiliateLock();
    assertTrue(lock.tryLock(Integer.MAX_VALUE, "a"));  // lock it first so that the next attempt will time out

    assertFalse(lock.tryLock(1, "b"));                 // should time out

    assertTrue(lock.tryLock(Integer.MAX_VALUE, "a"));  // will be reentrant
    lock.unlock("a");                                  // unlock once
    lock.unlock("a");                                  // unlock again, so next attempt will succeed

    assertTrue(lock.tryLock(Integer.MAX_VALUE, "b"));  // will succeed
    lock.unlock("b");
  }

  @Test
  public void testTryLock_withInterrupt() throws InterruptedException {
    final ReentrantAffiliateLock lock = new ReentrantAffiliateLock();
    assertTrue(lock.tryLock(Integer.MAX_VALUE, "a"));

    Thread.currentThread().interrupt();
    assertThatThrownBy(() -> {
      lock.tryLock(Integer.MAX_VALUE, "b");
    }).isExactlyInstanceOf(InterruptedException.class);
    assertFalse(Thread.interrupted());
  }

  @Test
  public void testUnlock_notLockedThrowsIllegalState() {
    final ReentrantAffiliateLock lock = new ReentrantAffiliateLock();
    assertThatThrownBy(() -> {
      lock.unlock("a");
    }).isExactlyInstanceOf(IllegalMonitorStateException.class).hasMessage("Not locked");
  }
  
  @Test
  public void testRelease_wrongAffiliateIllegalMonitorState() throws InterruptedException {
    final ReentrantAffiliateLock lock = new ReentrantAffiliateLock();
    assertTrue(lock.tryLock(Integer.MAX_VALUE, "a"));
    
    assertThatThrownBy(() -> {
      lock.unlock("b");
    }).isExactlyInstanceOf(IllegalMonitorStateException.class).hasMessage("Lock affiliated with a is being unlocked by b");
    
    lock.unlock("a");
  }
}

