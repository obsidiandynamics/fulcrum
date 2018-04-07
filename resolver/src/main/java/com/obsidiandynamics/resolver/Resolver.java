package com.obsidiandynamics.resolver;

import java.util.*;
import java.util.function.*;

import com.obsidiandynamics.func.*;

public final class Resolver {
  private static final Map<Scope, Scoped> scopes = new EnumMap<>(Scope.class);
  
  private static final Scope defaultScope = Scope.THREAD;
  
  static {
    for (Scope scope : Scope.values()) {
      scopes.put(scope, scope.make());
    }
  }
  
  public static final class ScopedResolver {
    private final Map<Class<?>, Supplier<? extends Object>> map;
    
    ScopedResolver(Map<Class<?>, Supplier<? extends Object>> map) {
      this.map = map;
    }
    
    private static <T> T supplyNull() { return null; }

    public <T> Supplier<T> lookup(Class<? super T> type) {
      return lookup(type, ScopedResolver::supplyNull);
    }
    
    public <T> Supplier<T> lookup(Class<? super T> type, Supplier<T> defaultValueSupplier) {
      final Object existing = map.get(type);
      if (existing != null) {
        return Classes.cast(existing);
      } else {
        return defaultValueSupplier;
      }
    }
    
    public <T> void assign(Class<T> type, Supplier<? extends T> supplier) {
      map.put(type, supplier);
    }
    
    public void reset() {
      map.clear();
    }
    
    public <T> Supplier<T> reset(Class<T> type) {
      return Classes.cast(map.remove(type));
    }
  }
  
  private Resolver() {}
  
  public static ScopedResolver scoped(Scope scope) {
    return new ScopedResolver(scopes.get(scope).get());
  }
  
  public static <T> Supplier<T> lookup(Class<? super T> type) {
    return scoped(defaultScope).lookup(type);
  }
  
  public static <T> Supplier<T> lookup(Class<? super T> type, Supplier<T> defaultValueSupplier) {
    return scoped(defaultScope).lookup(type, defaultValueSupplier);
  }
  
  public static <T> void assign(Class<T> type, Supplier<? extends T> supplier) {
    scoped(defaultScope).assign(type, supplier);
  }
  
  public static <T> Supplier<T> reset(Class<T> type) {
    return scoped(defaultScope).reset(type);
  }
  
  public static void reset() {
    scoped(defaultScope).reset();
  }
}
