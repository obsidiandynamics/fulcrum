package com.obsidiandynamics.func;

import java.util.function.*;

/**
 *  Represents a supplier of results. Unlike a conventional 
 *  {@link java.util.function.Supplier}, this variant
 *  is permitted to throw a checked exception.
 *
 *  @param <T> Result type.
 */
@FunctionalInterface
public interface CheckedSupplier<T, X extends Throwable> {
  T get() throws X;
  
  /**
   *  Coerces a conventional {@link Supplier} to a {@link CheckedSupplier} that throws a
   *  {@link RuntimeException}.
   *  
   *  @param <T> Input type.
   *  @param supplier The supplier to coerce.
   *  @return An equivalent {@link CheckedSupplier}.
   */
  static <T> CheckedSupplier<T, RuntimeException> toChecked(Supplier<? extends T> supplier) {
    return supplier::get;
  }

  /**
   *  Coerces {@link CheckedSupplier} that throws a {@link RuntimeException} to a 
   *  conventional {@link Supplier}.
   *  
   *  @param <T> Input type.
   *  @param supplier The supplier to coerce.
   *  @return An equivalent {@link Supplier}.
   */
  static <T> Supplier<T> toUnchecked(CheckedSupplier<? extends T, ? extends RuntimeException> supplier) {
    return supplier::get;
  }
}