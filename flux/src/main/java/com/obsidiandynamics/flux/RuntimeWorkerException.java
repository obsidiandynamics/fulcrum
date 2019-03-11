package com.obsidiandynamics.flux;

/**
 *  A way of shuttling a checked {@link FluxException} through a 
 *  {@link com.obsidiandynamics.worker.WorkerThread}
 *  (which does not permit checked exceptions) so that the former can be handled in the
 *  worker's on-shutdown handler.
 */
public final class RuntimeWorkerException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public RuntimeWorkerException(FluxException cause) { super(cause); }

  /**
   *  Conditionally unpacks the given exception, returning the cause, if the former
   *  is a {@link RuntimeWorkerException}. Otherwise, if the given exception
   *  is of another type, it is returned as is, with one caveat: if the exception is an
   *  {@link InterruptedException}, it is suppressed (returned as a {@code null}).
   *  
   *  @param exception The exception to consider.
   *  @return The conditionally unpacked exception.
   */
  public static Throwable unpackConditional(Throwable exception) {
    if (exception instanceof RuntimeWorkerException) {
      return exception.getCause();
    } else {
      return FluxSupport.suppressInterruptedException(exception);
    }
  }
}
