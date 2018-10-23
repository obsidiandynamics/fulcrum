package com.obsidiandynamics.retry;

import java.util.function.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.threads.*;

public final class Retry {
  private static final ExceptionHandler defaultExceptionHandler = ExceptionHandler.forPrintStream(System.err);
  
  private Predicate<Throwable> exceptionMatcher = isA(RuntimeException.class);
  private int attempts = 10;
  private int backoffMillis = 100;
  private ExceptionHandler faultHandler = defaultExceptionHandler;
  private ExceptionHandler errorHandler = defaultExceptionHandler;
  
  public static Predicate<Throwable> hasCauseThat(Predicate<Throwable> exceptionMatcher) {
    return new Predicate<Throwable>() {
      @Override
      public boolean test(Throwable t) {
        return exceptionMatcher.test(t.getCause());
      }
      
      @Override
      public String toString() {
        return "has cause that " + exceptionMatcher;
      }
    };
  }
  
  public static Predicate<Throwable> isA(Class<? extends Throwable> exceptionClass) {
    return new Predicate<Throwable>() {
      @Override
      public boolean test(Throwable t) {
        return exceptionClass.isInstance(t);
      }
      
      @Override
      public String toString() {
        return "is a " + exceptionClass.getSimpleName();
      }
    };
  }
  
  public Retry withExceptionMatcher(Predicate<Throwable> exceptionMatcher) {
    this.exceptionMatcher = exceptionMatcher;
    return this;
  }
  
  public Retry withAttempts(int attempts) {
    this.attempts = attempts;
    return this;
  }
  
  public Retry withBackoff(int backoffMillis) {
    this.backoffMillis = backoffMillis;
    return this;
  }
  
  public Retry withFaultHandler(ExceptionHandler faultHandler) {
    this.faultHandler = faultHandler;
    return this;
  }
  
  public Retry withErrorHandler(ExceptionHandler errorHandler) {
    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  public String toString() {
    return Retry.class.getSimpleName() + " [attempts=" + attempts + ", backoff=" + backoffMillis 
        + ", faultHandler=" + faultHandler + ", errorHandler=" + errorHandler 
        + ", exceptionMatcher=" + exceptionMatcher + "]";
  }
  
  public <X extends Throwable> void run(CheckedRunnable<X> operation) throws X {
    run(toVoidSupplier(operation));
  }
  
  private static <X extends Throwable> CheckedSupplier<Void, X> toVoidSupplier(CheckedRunnable<X> r) {
    return () -> {
      r.run();
      return null;
    };
  }
  
  public <T, X extends Throwable> T run(CheckedSupplier<? extends T, X> operation) throws X {
    for (int attempt = 0;; attempt++) {
      try {
        return operation.get();
      } catch (Throwable e) {
        if (exceptionMatcher.test(e)) {
          if (attempt == attempts - 1) {
            final String faultMessage = String.format("Fault (attempt #%,d of %,d): aborting", attempt + 1, attempts);
            errorHandler.onException(faultMessage, e);
            throw e;
          } else {
            final String retryMessage = String.format("Fault (attempt #%,d of %,d): retrying in %,d ms", 
                                                      attempt + 1, attempts, backoffMillis);
            faultHandler.onException(retryMessage, e);
            if (! Threads.sleep(backoffMillis)) {
              final String interruptMessage = String.format("Fault (attempt #%,d of %,d): aborting due to interrupt", 
                                                            attempt + 1, attempts);
              errorHandler.onException(interruptMessage, e);
              throw e;
            }
          }
        } else {
          throw e;
        }
      }
    }
  }
}
