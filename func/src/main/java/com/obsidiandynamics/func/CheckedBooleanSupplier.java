package com.obsidiandynamics.func;

import java.util.function.*;

/**
 *  Represents a supplier of a primitive {@code boolean} value. Unlike a conventional 
 *  {@link java.util.function.BooleanSupplier}, this variant is permitted to throw a 
 *  checked exception.
 *
 *  @param <X> Exception type.
 */
@FunctionalInterface
public interface CheckedBooleanSupplier<X extends Throwable> {
  boolean getAsBoolean() throws X;
  
  /**
   *  Coerces a conventional {@link BooleanSupplier} to a {@link CheckedBooleanSupplier} that throws a
   *  {@link RuntimeException}.
   *  
   *  @param supplier The supplier to coerce.
   *  @return An equivalent {@link CheckedBooleanSupplier}.
   */
  static CheckedBooleanSupplier<RuntimeException> toChecked(BooleanSupplier supplier) {
    return supplier::getAsBoolean;
  }

  /**
   *  Coerces {@link CheckedBooleanSupplier} that throws a {@link RuntimeException} to a 
   *  conventional {@link BooleanSupplier}.
   *  
   *  @param supplier The checked supplier to coerce.
   *  @return An equivalent {@link BooleanSupplier}.
   */
  static BooleanSupplier toUnchecked(CheckedBooleanSupplier<? extends RuntimeException> supplier) {
    return supplier::getAsBoolean;
  }
}