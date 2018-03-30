package com.obsidiandynamics.func;

import java.util.*;
import java.util.function.*;

@FunctionalInterface
public interface CheckedConsumer<T, X extends Exception> {
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
   *  @param <T> Input argument type.
   *  @param t Input argument.
   */
  static <T> void nop(T t) {}
  
  static <T> CheckedConsumer<T, RuntimeException> wrap(Consumer<? super T> consumer) {
    return consumer::accept;
  }
}
