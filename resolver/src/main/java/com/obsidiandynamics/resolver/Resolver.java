package com.obsidiandynamics.resolver;

import java.util.*;
import java.util.function.*;

public final class Resolver {
  private static final ThreadLocal<Map<Class<?>, Supplier<? extends Object>>> map = ThreadLocal.withInitial(HashMap::new);
  
  private Resolver() {}
  
  @SuppressWarnings("unchecked")
  public static <T> Supplier<T> lookup(Class<? super T> type, Supplier<T> defaultValueSupplier) {
    final Object existing = map.get().get(type);
    if (existing != null) {
      return (Supplier<T>) existing;
    } else {
      return defaultValueSupplier;
    }
  }
  
  public static <T> void assign(Class<T> type, Supplier<? extends T> supplier) {
    map.get().put(type, supplier);
  }
  
  @SuppressWarnings("unchecked")
  public static <T> Supplier<T> reset(Class<T> type) {
    return (Supplier<T>) map.get().remove(type);
  }
  
  public static void reset() {
    map.get().clear();
  }
}
