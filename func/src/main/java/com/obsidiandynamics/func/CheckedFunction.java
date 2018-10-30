package com.obsidiandynamics.func;

import java.util.*;
import java.util.function.*;

/**
 *  Represents a function that accepts one argument and produces a result. Unlike 
 *  a conventional {@link java.util.function.Function}, this variant
 *  is permitted to throw a checked exception.
 *
 *  @param <T> Input type.
 *  @param <R> Result type.
 */
@FunctionalInterface
public interface CheckedFunction<T, R, X extends Throwable> {
  R apply(T t) throws X;

  default <V> CheckedFunction<V, R, X> compose(CheckedFunction<? super V, ? extends T, ? extends X> before) {
    Objects.requireNonNull(before);
    return v -> apply(before.apply(v));
  }

  default <V> CheckedFunction<T, V, X> andThen(CheckedFunction<? super R, ? extends V, ? extends X> after) {
    Objects.requireNonNull(after);
    return t -> after.apply(apply(t));
  }
  
  /**
   *  Coerces a conventional {@link Function} to a {@link CheckedFunction} that throws
   *  a {@link RuntimeException}.
   *  
   *  @param <T> Input type.
   *  @param <R> Result type.
   *  @param function The function to coerce.
   *  @return An equivalent {@link CheckedFunction}.
   */
  static <T, R> CheckedFunction<T, R, RuntimeException> toChecked(Function<? super T, ? extends R> function) {
    return function::apply;
  }
  
  /**
   *  Coerces a {@link CheckedFunction} that throws a {@link RuntimeException} to a 
   *  conventional {@link Function}.
   *  
   *  @param <T> Input type.
   *  @param <R> Result type.
   *  @param function The function to coerce.
   *  @return An equivalent {@link Function}.
   */
  static <T, R> Function<T, R> toUnchecked(CheckedFunction<? super T, ? extends R, ? extends RuntimeException> function) {
    return function::apply;
  }
}