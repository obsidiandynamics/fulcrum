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
 *  operations on the default scope — {@link Scope#THREAD}.<p>
 *  
 *  This class is thread-safe.
 */
public final class Resolver {
  private static final Map<Scope, Scoped> scopes = new EnumMap<>(Scope.class);
  
  private static final Scope defaultScope = Scope.THREAD;
  
  /**
   *  Obtains the default scope.
   *  
   *  @return The default {@link Scope}.
   */
  public static Scope defaultScope() {
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

    /**
     *  Looks up a value for a given class {@code type}. If the mapping is absent, a
     *  null-resolving {@link Supplier} is returned.
     *
     *  @param <T> Value type.
     *  @param type The class type.
     *  @return A {@link Supplier} instance that will produce an instance of the mapped,
     *          type, or a {@link Supplier} of {@code null} if the type isn't mapped.
     */
    public <T> Supplier<T> lookup(Class<? super T> type) {
      return lookup(type, ScopedResolver::supplyNull);
    }
    
    /**
     *  Looks up a value for a given class {@code type}. If the mapping is absent, the
     *  provided {@code defaultValueSupplier} is returned. (The underlying type
     *  will remain unassigned.)
     *  
     *  @param <T> Value type.
     *  @param type The class type.
     *  @param defaultValueSupplier A {@link Supplier} of a default value.
     *  @return A {@link Supplier} instance that will produce an instance of the mapped,
     *          type, or the {@code defaultValueSupplier} if the type isn't mapped. 
     */
    public <T> Supplier<T> lookup(Class<? super T> type, Supplier<T> defaultValueSupplier) {
      final Object existing = map.get(type);
      if (existing != null) {
        return Classes.cast(existing);
      } else {
        return defaultValueSupplier;
      }
    }
    
    /**
     *  Assigns a {@link Supplier} for a given class {@code type}, to be returned for all
     *  future invocations of {@link ScopedResolver#lookup}.
     *  
     *  @param <T> Value type.
     *  @param type The class type.
     *  @param supplier The supplier for the mapped {@code type}.
     *  @return The previously assigned {@link Supplier}, or {@code null} if this mapping was
     *          not assigned.
     */
    public <T> T assign(Class<T> type, Supplier<? extends T> supplier) {
      return Classes.cast(map.put(type, Classes.cast(supplier)));
    }
    
    /**
     *  Resets all mappings within this contextual scope. This leaves all mappings in an
     *  unassigned state.
     */
    public void reset() {
      map.clear();
    }
    
    /**
     *  Resets the mapping for the given {@code type}, leaving it in an unassigned state.
     *  
     *  @param <T> Value type.
     *  @param type The class type.
     *  @return The previously assigned {@link Supplier}, or {@code null} if this mapping was
     *          not assigned.
     */
    public <T> Supplier<T> reset(Class<T> type) {
      return Classes.cast(map.remove(type));
    }
  }
  
  private Resolver() {}
  
  /**
   *  Obtains a {@link ScopedResolver} for the given {@code scope}.
   *  
   *  @param scope The scope.
   *  @return The scoped resolver.
   */
  public static ScopedResolver scope(Scope scope) {
    return new ScopedResolver(scopes.get(scope).map());
  }
  
  /**
   *  Equivalent of invoking {@link ScopedResolver#lookup(Class)} for the <em>default</em>
   *  scope, i.e. {@code Resolver.scope(Resolver.defaultScope()).lookup(Class)}.
   *  
   *  @see ScopedResolver#lookup(Class)
   *  
   *  @param <T> Value type.
   *  @param type The class type.
   *  @return A {@link Supplier} instance that will produce an instance of the mapped,
   *          type, or a {@link Supplier} of {@code null} if the type isn't mapped.
   */
  public static <T> Supplier<T> lookup(Class<? super T> type) {
    return scope(defaultScope).lookup(type);
  }
  
  /**
   *  Equivalent of invoking {@link ScopedResolver#lookup(Class, Supplier)} for the <em>default</em>
   *  scope, i.e. {@code Resolver.scope(Resolver.defaultScope()).lookup(Class, Supplier)}.
   *  
   *  @see ScopedResolver#lookup(Class, Supplier)
   *  
   *  @param <T> Value type.
   *  @param type The class type.
   *  @param defaultValueSupplier A {@link Supplier} of a default value.
   *  @return A {@link Supplier} instance that will produce an instance of the mapped,
   *          type, or the {@code defaultValueSupplier} if the type isn't mapped. 
   */
  public static <T> Supplier<T> lookup(Class<? super T> type, Supplier<T> defaultValueSupplier) {
    return scope(defaultScope).lookup(type, defaultValueSupplier);
  }
  
  /**
   *  Equivalent of invoking {@link ScopedResolver#assign(Class, Supplier)} for the <em>default</em>
   *  scope, i.e. {@code Resolver.scope(Resolver.defaultScope()).assign(Class, Supplier)}.
   *  
   *  @see ScopedResolver#assign(Class, Supplier)
   *
   *  @param <T> Value type.
   *  @param type The class type.
   *  @param supplier The supplier for the mapped {@code type}.
   *  @return The previously assigned {@link Supplier}, or {@code null} if this mapping was
   *          not assigned.
   */
  public static <T> T assign(Class<T> type, Supplier<? extends T> supplier) {
    return scope(defaultScope).assign(type, supplier);
  }
  
  /**
   *  Equivalent of invoking {@link ScopedResolver#reset(Class)} for the <em>default</em>
   *  scope, i.e. {@code Resolver.scope(Resolver.defaultScope()).reset(Class)}.
   *  
   *  @see ScopedResolver#reset(Class)
   *
   *  @param <T> Value type.
   *  @param type The class type.
   *  @return The previously assigned {@link Supplier}, or {@code null} if this mapping was
   *          not assigned.
   */
  public static <T> Supplier<T> reset(Class<T> type) {
    return scope(defaultScope).reset(type);
  }
  
  /**
   *  Equivalent of invoking {@link ScopedResolver#reset()} for the <em>default</em>
   *  scope, i.e. {@code Resolver.scope(Resolver.defaultScope()).reset()}.
   *  
   *  @see ScopedResolver#reset()
   */ 
  public static void reset() {
    scope(defaultScope).reset();
  }
}
