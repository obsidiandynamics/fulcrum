package com.obsidiandynamics.func;

import java.util.*;
import java.util.function.*;

@FunctionalInterface
public interface CheckedFunction<T, R, X extends Exception> {
  R apply(T t) throws X;

  default <V> CheckedFunction<V, R, X> compose(CheckedFunction<? super V, ? extends T, ? extends X> before) {
    Objects.requireNonNull(before);
    return v -> apply(before.apply(v));
  }

  default <V> CheckedFunction<T, V, X> andThen(CheckedFunction<? super R, ? extends V, ? extends X> after) {
    Objects.requireNonNull(after);
    return t -> after.apply(apply(t));
  }
  
  static <T, R> CheckedFunction<T, R, RuntimeException> wrap(Function<? super T, ? extends R> function) {
    return function::apply;
  }
}