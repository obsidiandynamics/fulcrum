package com.obsidiandynamics.flux;

import java.util.function.*;

/**
 *  An exception thrown from a {@link DiscreteStage} within the {@link Flux} pipeline.
 */
public class FluxException extends Exception {
  private static final long serialVersionUID = 1L;

  public FluxException(Throwable cause) {
    this(null, cause);
  }

  public FluxException(String m) {
    this(m, null);
  }

  public FluxException(String m, Throwable cause) {
    super(m, cause);
  }
  
  /**
   *  Conditionally wraps a given exception, ensuring that the resulting exception
   *  is an instance of a {@link FluxException}. If the exception is already a {@link FluxException},
   *  it is returned as-is, without further encapsulation. If the argument is an
   *  {@link InterruptedException}, it is mapped to a {@code} null. (Suppressed, in other words.) 
   *  Similarly, a {@code null} argument is also returned as {@code null}.
   *  
   *  @param exception The exception to wrap (can be {@code null}).
   *  @return The wrapped exception, possibly {@code null}.
   */
  public static final FluxException wrap(Throwable exception) {
    return wrapException(exception, FluxException.class, FluxException::new);
  }
  
  private static <X extends Throwable> X wrapException(Throwable exception, 
                                                       Class<X> testType, 
                                                       Function<Throwable, ? extends X> exceptionWrapper) {
    if (FluxSupport.suppressInterruptedException(exception) == null) {
      return null;
    } else if (testType.isInstance(exception)) {
      return testType.cast(exception);
    } else {
      return exceptionWrapper.apply(exception);
    }
  }
}
