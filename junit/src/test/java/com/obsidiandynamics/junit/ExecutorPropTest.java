package com.obsidiandynamics.junit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.junit.*;
import org.mockito.*;

public final class ExecutorPropTest {
  @Rule
  public final ExecutorProp prop = new ExecutorProp(4, ExecutorPropTest::mockExecutorService);
  
  private static ExecutorService lastExecutor;
  
  private static ExecutorProp lastProp;
  
  @AfterClass
  public static void afterClass() {
    assertNotNull(lastExecutor);
    assertTrue(lastExecutor.isShutdown());
    lastExecutor = null;
    
    assertNotNull(lastProp);
    try {
      lastProp.getExecutor();
      fail();
    } catch (IllegalStateException e) {
      assertEquals("Executor not running", e.getMessage());
    }
  }
  
  private static ExecutorService mockExecutorService(int parallelism) {
    final ExecutorService exec = mock(ExecutorService.class, Answers.CALLS_REAL_METHODS);
    final AtomicBoolean running = new AtomicBoolean(true);
    when(exec.shutdownNow()).then(invocation -> {
      running.set(false);
      return null;
    });
    when(exec.isShutdown()).then(invocation -> ! running.get());
    return exec;
  }
  
  @Test
  public void test() {
    final ExecutorService exec = prop.getExecutor();
    assertNotNull(exec);
    assertFalse(exec.isShutdown());
    lastExecutor = exec;
    lastProp = prop;
  }
}
