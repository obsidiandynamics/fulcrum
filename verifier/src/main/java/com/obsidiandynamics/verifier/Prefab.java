package com.obsidiandynamics.verifier;

import static com.obsidiandynamics.func.Functions.*;

import java.util.*;

import nl.jqno.equalsverifier.internal.prefabvalues.*;
import nl.jqno.equalsverifier.internal.prefabvalues.factories.*;
import nl.jqno.equalsverifier.internal.reflection.*;

/**
 *  A helper for working with {@link PrefabValues} and {@link FactoryCache} from Jan Ouwens'
 *  EqualsVerifier, combining both classes into a single API. <p>
 *  
 *  This class is thread-safe when the underlying {@link PrefabValues} and {@link FactoryCache} 
 *  instances are accessed via the public API. Conversely, use {@link #getLock()} if accessing
 *  these instances directly.
 */
public final class Prefab {
  private static final Prefab instance = new Prefab();
  
  /**
   *  Obtains a mutable, application-wide {@link Prefab} singleton for shared use.
   *  
   *  @return A {@link Prefab} singleton instance.
   */
  public static Prefab getInstance() {
    return instance;
  }
  
  private final Object lock = new Object();
  
  private final FactoryCache factoryCache;
  
  private final PrefabValues prefabValues;
  
  /**
   *  Creates a new, non-shared instance.
   */
  public Prefab() {
    final FactoryCache initialCache = new FactoryCache();
    factoryCache = JavaApiPrefabValues.build().merge(initialCache);
    prefabValues = new PrefabValues(factoryCache);
  }
  
  /**
   *  Obtains the lock object that is used to provide exclusivity over the underlying
   *  {@link FactoryCache} and {@link PrefabValues} instance. <p>
   *  
   *  In the scenario where {@link Prefab} is used as shared instance, all direct operations
   *  on the underlying {@link FactoryCache} and {@link PrefabValues} must be guarded
   *  by a lock for thread safety.
   *  
   *  @return The lock object.
   */
  public Object getLock() {
    return lock;
  }
  
  /**
   *  Obtains the underlying {@link FactoryCache} instance for direct modification. <p>
   *  
   *  The returned instance must by guarded by a lock in a concurrent access scenario.
   *  
   *  @see Prefab#getLock()
   *  
   *  @return The underlying {@link FactoryCache} instance.
   */
  public FactoryCache getFactoryCache() {
    return factoryCache;
  }
  
  /**
   *  Obtains the underlying {@link PrefabValues} instance for direct modification. <p>
   *  
   *  The returned instance must by guarded by a lock in a concurrent access scenario.
   *  
   *  @see Prefab#getLock()
   *  
   *  @return The underlying {@link PrefabValues} instance.
   */
  public PrefabValues getPrefabValues() {
    return prefabValues;
  }
  
  /**
   *  Creates a shallow copy of a given value. (Arrays are returned
   *  as-as; other types are copied with {@link ObjectAccessor}).
   *  
   *  @param <T> Value type.
   *  @param value The value.
   *  @return Its copy.
   */
  public static <T> T shallowCopy(T value) {
    mustExist(value);
    
    if (value.getClass().isArray()) {
      return value;
    } else {
      return ObjectAccessor.of(value).copy();
    }
  }
  
  /**
   *  Registers a red-black instance pair.
   *  
   *  @param <T> Value type.
   *  @param type The type of the pair.
   *  @param red The red instance.
   *  @param black The black instance.
   */
  public <T> void register(Class<T> type, T red, T black) {
    mustExist(type);
    mustExist(red);
    mustExist(black);
    
    final T redCopy = shallowCopy(red);
    synchronized (lock) {
      factoryCache.put(type, (tag, prefabValues, typeStack) -> new Tuple<>(red, black, redCopy));
    }
  }
  
  /**
   *  Acquires a 'red' instance for the given type.
   *  
   *  @param <T> Value type.
   *  @param type The type.
   *  @return The 'red' instance.
   */
  public <T> T red(Class<T> type) {
    mustExist(type);
    synchronized (lock) {
      return prefabValues.giveRed(new TypeTag(type));
    }
  }
  
  /**
   *  Acquires a 'black' instance for the given type.
   *  
   *  @param <T> Value type.
   *  @param type The type.
   *  @return The 'black' instance.
   */
  public <T> T black(Class<T> type) {
    mustExist(type);
    synchronized (lock) {
      return prefabValues.giveBlue(new TypeTag(type));
    }
  }
}
