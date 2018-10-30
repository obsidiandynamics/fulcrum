package com.obsidiandynamics.func;

import java.util.*;
import java.util.function.*;

/**
 *  Represents an operation that accepts a single input argument and returns no
 *  result. Unlike a conventional {@link java.util.function.Consumer}, this variant
 *  is permitted to throw a checked exception.
 *
 *  @param <T> Input type.
 *  @param <X> Exception type.
 */
@FunctionalInterface
public interface CheckedConsumer<T, X extends Throwable> {
  void accept(T t) throws X;

  default CheckedConsumer<T, X> andThen(CheckedConsumer<? super T, ? extends X> after) {
    Objects.requireNonNull(after);
    return t -> { 
      accept(t); after.accept(t);
    };
  }
  
  /**
   *  A no-op.
   *  
   *  @param <T> Input type.
   *  @param t Input argument.
   */
  static <T> void nop(T t) {}
  
  /**
   *  Coerces a conventional {@link Consumer} to a {@link CheckedConsumer} that throws a
   *  {@link RuntimeException}.
   *  
   *  @param <T> Input type.
   *  @param consumer The consumer to coerce.
   *  @return An equivalent {@link CheckedConsumer}.
   */
  static <T> CheckedConsumer<T, RuntimeException> toChecked(Consumer<? super T> consumer) {
    return consumer::accept;
  }
  
  /**
   *  Coerces a {@link CheckedConsumer} that throws a {@link RuntimeException} to a
   *  conventional {@link Consumer}.
   *  
   *  @param <T> Input type.
   *  @param consumer The consumer to coerce.
   *  @return An equivalent {@link Consumer}.
   */
  static <T> Consumer<T> toUnchecked(CheckedConsumer<? super T, ? extends RuntimeException> consumer) {
    return consumer::accept;
  }
}
