package com.obsidiandynamics.resolver;

import java.util.*;
import java.util.function.*;

import com.obsidiandynamics.func.*;

/**
 *  <em>Resolver</em> is an implementation of a Contextual Service Locator (CSL) pattern, enabling 
 *  distinct parts of an application that are not otherwise directly aware of one another to 
 *  share services. However, unlike a traditional Service Locator, a CSL isn't 'static',
 *  and so doesn't negatively impact qualities such as testability or maintainability.<p>
 *  
 *  The {@link Resolver} class acts as a central point for assigning and looking up values 
 *  across a range of {@link Scope}s.<p>
 *  
 *  A scope is accessed by calling {@link Resolver#scope(Scope)}, further exposing operations
 *  such as {@link ScopedResolver#lookup(Class, Supplier)}, 
 *  {@link ScopedResolver#assign(Class, Supplier)} and {@link ScopedResolver#reset()}.<p>
 *  
 *  Calling static methods on {@link Resolver} without specifying a scope will default to 
 *  operations on the default scope — {@link Scope#THREAD}.
 */
public final class Resolver {
  private static final Map<Scope, Scoped> scopes = new EnumMap<>(Scope.class);
  
  private static final Scope defaultScope = Scope.THREAD;
  
  public static Scope getDefaultScope() {
    return defaultScope;
  }
  
  static {
    for (Scope scope : Scope.values()) {
      scopes.put(scope, scope.make());
    }
  }
  
  /**
   *  A contextual resolver that has been tied to a specific {@link Scope}.
   */
  public static final class ScopedResolver {
    private final Map<Class<?>, Supplier<Object>> map;
    
    ScopedResolver(Map<Class<?>, Supplier<Object>> map) {
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
      map.put(type, Classes.cast(supplier));
    }
    
    public void reset() {
      map.clear();
    }
    
    public <T> Supplier<T> reset(Class<T> type) {
      return Classes.cast(map.remove(type));
    }
  }
  
  private Resolver() {}
  
  public static ScopedResolver scope(Scope scope) {
    return new ScopedResolver(scopes.get(scope).map());
  }
  
  public static <T> Supplier<T> lookup(Class<? super T> type) {
    return scope(defaultScope).lookup(type);
  }
  
  public static <T> Supplier<T> lookup(Class<? super T> type, Supplier<T> defaultValueSupplier) {
    return scope(defaultScope).lookup(type, defaultValueSupplier);
  }
  
  public static <T> void assign(Class<T> type, Supplier<? extends T> supplier) {
    scope(defaultScope).assign(type, supplier);
  }
  
  public static <T> Supplier<T> reset(Class<T> type) {
    return scope(defaultScope).reset(type);
  }
  
  public static void reset() {
    scope(defaultScope).reset();
  }
}
