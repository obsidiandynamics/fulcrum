package com.obsidiandynamics.threads;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.*;

import org.junit.*;

public final class CloseableExecutorTest {
  @Test
  public void testGetAndClose() {
    final ExecutorService executor = mock(ExecutorService.class);
    final CloseableExecutor closeableExecutor = CloseableExecutor.of(executor);
    assertSame(executor, closeableExecutor.get());
    
    verify(executor, never()).shutdown();
    closeableExecutor.close();
    verify(executor).shutdown();
  }
}
