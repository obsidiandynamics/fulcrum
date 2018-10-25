package com.obsidiandynamics.junit;

import java.util.concurrent.*;
import java.util.function.*;

import org.junit.rules.*;
import org.junit.runner.*;
import org.junit.runners.model.*;

/**
 *  A JUnit rule that provides an {@link ExecutorService} for the duration of the test.
 */
public final class ExecutorProp implements TestRule {
  private final Supplier<ExecutorService> executorServiceFactory;
  
  private ExecutorService executor;
  
  public ExecutorProp(int parallelism, IntFunction<ExecutorService> executorServiceFactory) {
    this(() -> executorServiceFactory.apply(parallelism));
  }
  
  public ExecutorProp(Supplier<ExecutorService> executorServiceFactory) {
    this.executorServiceFactory = executorServiceFactory;
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        executor = executorServiceFactory.get();
        try {
          base.evaluate();
        } finally {
          executor.shutdownNow();
          executor = null;
        }
      }
    };
  }
  
  public ExecutorService getExecutor() {
    if (executor == null) throw new IllegalStateException("Executor not running");
    return executor;
  }
}
