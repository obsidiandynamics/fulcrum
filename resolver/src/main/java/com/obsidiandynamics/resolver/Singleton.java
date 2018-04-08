package com.obsidiandynamics.resolver;

import java.util.function.*;

/**
 *  A convenience class for generating a {@link Supplier} of singleton values,
 *  with an option of either an <em>eager</em> or a <em>lazy</em> supplier.
 */
public final class Singleton {
  private static class EagerSingleton<T> implements Supplier<T> {
    private final T value;
    
    EagerSingleton(T value) {
      this.value = value;
    }

    @Override
    public T get() {
      return value;
    }
  }
  
  private static class LazySingleton<T> implements Supplier<T> {
    private T value;
    private Supplier<T> valueSupplier;
    
    LazySingleton(Supplier<T> valueSupplier) {
      this.valueSupplier = valueSupplier;
    }

    @Override
    public synchronized T get() {
      if (value == null) {
        value = valueSupplier.get();
        valueSupplier = null;
      }
      return value;
    }
  }
  
  private Singleton() {}
  
  /**
   *  Produces an <em>eager</em> supplier of the given instance. The same value
   *  will be returned for all future invocations of {@code get()} on the resulting
   *  {@link Supplier} instance.<p>
   *  
   *  This supplier is thread-safe.
   *  
   *  @param <T> Value type.
   *  @param value The singleton value.
   *  @return The singleton {@link Supplier}.
   */
  public static <T> Supplier<T> of(T value) {
    return new EagerSingleton<>(value);
  }
  
  /**
   *  Produces a <em>lazy</em> supplier based on a given {@code valueSupplier}. The value
   *  will be lazily instantiated upon first use (the first call to {@code get()}), and
   *  will subsequently be reused for all future calls to {@code get()}.<p>
   *  
   *  This supplier is thread-safe.
   *  
   *  @param <T> Value type.
   *  @param valueSupplier A factory for creating a new instance of the value type.
   *  @return The singleton {@link Supplier}.
   */
  public static <T> Supplier<T> of(Supplier<T> valueSupplier) {
    return new LazySingleton<>(valueSupplier);
  }
}
