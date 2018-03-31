package com.obsidiandynamics.retry;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.threads.*;

public final class Retry {
  private static final ExceptionHandler defaultExceptionHandler = ExceptionHandler.forPrintStream(System.err);
  
  private Class<? extends RuntimeException> exceptionClass = RuntimeException.class;
  private int attempts = 10;
  private int backoffMillis = 100;
  private ExceptionHandler faultHandler = defaultExceptionHandler;
  private ExceptionHandler errorHandler = defaultExceptionHandler;
  
  public Retry withExceptionClass(Class<? extends RuntimeException> exceptionClass) {
    this.exceptionClass = exceptionClass;
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
        + ", exceptionClass=" + exceptionClass.getSimpleName() + "]";
  }
  
  public <X extends Exception> void run(CheckedRunnable<X> operation) throws X {
    run(toVoidSupplier(operation));
  }
  
  private static <X extends Exception> CheckedSupplier<Void, X> toVoidSupplier(CheckedRunnable<X> r) {
    return () -> {
      r.run();
      return null;
    };
  }
  
  public <T, X extends Exception> T run(CheckedSupplier<? extends T, X> operation) throws X {
    for (int attempt = 0;; attempt++) {
      try {
        return operation.get();
      } catch (RuntimeException e) {
        if (exceptionClass.isInstance(e)) {
          final String message = String.format("Fault: (attempt #%,d of %,d)", attempt + 1, attempts);
          if (attempt == attempts - 1) {
            errorHandler.onException(message, e);
            throw e;
          } else {
            if (Threads.sleep(backoffMillis)) {
              faultHandler.onException(message, e);
            } else {
              errorHandler.onException(message, e);
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
