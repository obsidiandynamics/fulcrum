package com.obsidiandynamics;

import static org.junit.Assert.*;

import java.io.*;
import java.util.concurrent.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;
import com.obsidiandynamics.threads.*;

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
      assertFalse(Threads.runUninterruptedly(() -> {
        throw new InterruptedException("test interrupted");
      }));
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
  
  private static class TestRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    TestRuntimeException(Throwable cause) { super(cause); }
  }
  
  @Test
  public void testWrapInRuntimeExceptionNormal() {
    Threads.wrapInRuntimeException(() -> {}, TestRuntimeException::new);
  }
  
  @Test(expected=TestRuntimeException.class)
  public void testWrapInRuntimeExceptionThrown() {
    Threads.wrapInRuntimeException(() -> {
      throw new IOException("test exception");
    }, TestRuntimeException::new);
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
  public void testAwaitLatch() {
    final CountDownLatch latch = new CountDownLatch(1);
    new Thread(() -> {
      latch.countDown();
    }).start();
    Threads.await(latch);
  }
}
