package com.obsidiandynamics.func;

import java.util.function.*;

@FunctionalInterface
public interface CheckedSupplier<T, X extends Exception> {
  T get() throws X;
  
  static <T> CheckedSupplier<T, RuntimeException> wrap(Supplier<? extends T> supplier) {
    return supplier::get;
  }
}