package com.obsidiandynamics.resolver;

import java.util.function.*;

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
  
  public static <T> Supplier<T> of(T value) {
    return new EagerSingleton<>(value);
  }
  
  public static <T> Supplier<T> of(Supplier<T> valueSupplier) {
    return new LazySingleton<>(valueSupplier);
  }
}
