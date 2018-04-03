package com.obsidiandynamics.func;

import java.util.function.*;

public final class Exceptions {
  private Exceptions() {}
  
  @FunctionalInterface
  public interface ExceptionWrapper<X extends Throwable> extends Function<Throwable, X> {}
  
  public static <X extends Throwable> void wrap(CheckedRunnable<?> runnable, ExceptionWrapper<X> wrapper) throws X {
    wrap(() -> {
      runnable.run();
      return null;
    }, wrapper);
  }
  
  public static <T, X extends Throwable> T wrap(CheckedSupplier<? extends T, ?> supplier, ExceptionWrapper<X> wrapper) throws X {
    try {
      return supplier.get();
    } catch (Throwable e) {
      throw wrapper.apply(e);
    }
  }
  
  public static <X extends Throwable> CheckedRunnable<X> doThrow(Supplier<X> exceptionMaker) {
    return () -> { throw exceptionMaker.get(); };
  }
}
