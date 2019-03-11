package com.obsidiandynamics.flux;

public final class FluxSupport {
  private FluxSupport() {}
  
  /**
   *  Returns the given {@code exception} if the latter is not an instance of 
   *  {@link InterruptedException} or a {@code null}. Otherwise, a {@code null} 
   *  is returned. <p>
   *  
   *  This method can be used to conveniently suppress an {@link InterruptedException}
   *  inline, avoiding unsightly branching.
   *  
   *  @param exception The exception to inspect.
   *  @return The given {@code exception} if the latter isn't an {@link InterruptedException}.
   */
  public static Throwable suppressInterruptedException(Throwable exception) {
    return exception == null || exception instanceof InterruptedException ? null : exception;
  }
}
