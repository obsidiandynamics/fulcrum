package com.obsidiandynamics.func;

import java.util.function.*;

/**
 *  Helper utilities for working with exceptions.
 */
public final class Exceptions {
  private Exceptions() {}
  
  /**
   *  Evaluates the given {@code runnable}. If an exception is generated, it will be wrapped using the
   *  thrown exception using the specified {@code wrapper}.
   *  
   *  @param <X> Exception type.
   *  @param runnable The runnable whose exceptions to trap.
   *  @param wrapper The handler for trapped exceptions, returning a wrapped exception of type {@code X}.
   *  @throws X If an exception occurs.
   */
  public static <X extends Throwable> void wrap(CheckedRunnable<?> runnable, 
                                                Function<Throwable, X> wrapper) throws X {
    wrap(() -> {
      runnable.run();
      return null;
    }, wrapper);
  }
  
  /**
   *  Evaluates the given {@code runnable}. If an exception is generated, it will be wrapped using the
   *  thrown exception using the specified {@code wrapper}.<p>
   *  
   *  This variant is an alias of {@link #wrap(CheckedRunnable, Function)} that avoids method ambiguity 
   *  when a {@link CheckedRunnable} is being used.
   *  
   *  @param <X> Exception type.
   *  @param runnable The runnable whose exceptions to trap.
   *  @param wrapper The handler for trapped exceptions, returning a wrapped exception of type {@code X}.
   *  @throws X If an exception occurs.
   */
  public static <X extends Throwable> void wrapRunnable(CheckedRunnable<?> runnable, 
                                                        Function<Throwable, X> wrapper) throws X {
    wrap(runnable, wrapper);
  }
  
  /**
   *  Evaluates the given {@code supplier}, returning the supplied value if successful, or otherwise wrapping the
   *  thrown exception using the specified {@code wrapper}.
   *  
   *  @param <T> Return type.
   *  @param <X> Exception type.
   *  @param supplier The supplier whose exceptions to trap.
   *  @param wrapper The handler for trapped exceptions, returning a wrapped exception of type {@code X}.
   *  @return The result of evaluating the supplier.
   *  @throws X If an exception occurs.
   */
  public static <T, X extends Throwable> T wrap(CheckedSupplier<? extends T, ?> supplier, 
                                                Function<Throwable, X> wrapper) throws X {
    return wrapStrict(supplier, wrapper);
  }

  /**
   *  A strict form of {@link #wrap(CheckedRunnable, Function)} that requires that the exception
   *  wrapper takes a subtype of the exception thrown by the runnable block.
   *  
   *  @param <W> Exception type thrown by the runnable, and input to the wrapper.
   *  @param <X> Exception type thrown by the wrapper.
   *  @param runnable The runnable whose exception to trap.
   *  @param wrapper The handler for trapped exceptions, returning a wrapped exception of type {@code X}.
   *  @throws X If an exception occurs.
   */
  public static <W extends Throwable, X extends Throwable> void wrapStrict(CheckedRunnable<W> runnable, 
                                                                           Function<? super W, X> wrapper) throws X {
    wrapStrict(() -> {
      runnable.run();
      return null;
    }, wrapper);
  }

  /**
   *  A strict form of {@link #wrap(CheckedSupplier, Function)} that requires that the exception
   *  wrapper takes a subtype of the exception thrown by the supplier.
   *  
   *  @param <T> Return type.
   *  @param <W> Exception type thrown by the runnable, and input to the wrapper.
   *  @param <X> Exception type thrown by the wrapper.
   *  @param supplier The supplier whose exception to trap.
   *  @param wrapper The handler for trapped exceptions, returning a wrapped exception of type {@code X}.
   *  @return The result of evaluating the supplier.
   *  @throws X If an exception occurs.
   */
  public static <T, W extends Throwable, X extends Throwable> T wrapStrict(CheckedSupplier<? extends T, W> supplier, 
                                                                           Function<? super W, X> wrapper) throws X {
    try {
      return supplier.get();
    } catch (Throwable e) {
      throw wrapper.apply(Classes.cast(e));
    }
  }
  
  /**
   *  Evaluates the given {@code supplier}, returning the supplied value if successful, or otherwise wrapping the
   *  thrown exception using the specified {@code wrapper}.<p>
   *  
   *  This variant is an alias of {@link #wrap(CheckedSupplier, Function)} that avoids method ambiguity 
   *  when a {@link CheckedSupplier} is being used.
   *  
   *  @param <T> Return type.
   *  @param <X> Exception type.
   *  @param supplier The supplier whose exceptions to trap.
   *  @param wrapper The handler for trapped exceptions, returning a wrapped exception of type {@code X}.
   *  @return The result of evaluating the supplier.
   *  @throws X If an exception occurs.
   */
  public static <T, X extends Throwable> T wrapSupplier(CheckedSupplier<? extends T, ?> supplier, 
                                                        Function<Throwable, X> wrapper) throws X {
    return wrap(supplier, wrapper);
  }
  
  /**
   *  Produces a {@link CheckedRunnable} that, when invoked, throws an exception generated by the
   *  given {@code exceptionMaker}.
   *  
   *  @param <X> Exception type.
   *  @param exceptionMaker The exception generator.
   *  @return A {@link CheckedRunnable} instance that throws the generated exception.
   */
  public static <X extends Throwable> CheckedRunnable<X> doThrow(Supplier<X> exceptionMaker) {
    return () -> { throw exceptionMaker.get(); };
  }
}
