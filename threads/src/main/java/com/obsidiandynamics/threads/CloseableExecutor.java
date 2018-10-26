package com.obsidiandynamics.threads;

import java.util.concurrent.*;

/**
 *  A wrapper for a short-lived {@link ExecutorService} that should be disposed in
 *  a try-with-resources statement. Invoking {@link #close()} has the effect of calling
 *  {@link ExecutorService#shutdown()}.
 */
public final class CloseableExecutor implements AutoCloseable {
  private final ExecutorService executor;
  
  private CloseableExecutor(ExecutorService executor) {
    this.executor = executor;
  }
  
  public ExecutorService get() {
    return executor;
  }

  @Override
  public void close() {
    executor.shutdown();
  }

  public static CloseableExecutor of(ExecutorService executor) {
    return new CloseableExecutor(executor);
  }
}
