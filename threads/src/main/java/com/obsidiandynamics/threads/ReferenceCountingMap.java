package com.obsidiandynamics.threads;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 *  A concurrent, automatic reference-counting map. <p>
 *  
 *  Unlike a conventional {@link Map} that offers manual lifecycle management of mapped 
 *  values using {@code put()} and {@code delete()} methods, a {@link ReferenceCountingMap} 
 *  manages the lifecycle of mapped values based on the number of scoped references maintained
 *  by the consuming application. <p>
 *  
 *  A value is initially inserted into the map when the key is first <em>scoped</em> by the
 *  caller, by invoking {@link #scope(Object, Supplier)}. The {@link Supplier} is used to
 *  instantiate the value prior to insertion, and the instantiated value is returned to the
 *  caller. Subsequent invocations increment the reference count but do not result in 
 *  changes to the mapping. When the caller is finished with the value, it will <em>de-scope</em>
 *  it by invoking {@link #descope(Object)} for its key. When the reference count reaches zero,
 *  the mapping is automatically expunged from the map. <p>
 *  
 *  This class is thread-safe. Value instantiation, insertion and expulsion are performed 
 *  atomically; the caller need only ensure that the value is de-scoped in a manner that is consistent
 *  with the scope acquisition count. This is best achieved using a try-with-resources construct. 
 *  Multiple threads may hold references to a mapped value concurrently; the mapping will persist 
 *  for as long as at least one thread maintains a scoped reference. <p>
 *  
 *  The reference counting mechanism is purely semantic, driven by explicit instruction and 
 *  predicated upon unconditional cooperation with the consumer application threads. 
 *  The {@link ReferenceCountingMap} is not aware of any strong references to the mapped values that
 *  the application might hold outside of the scoped operations. It is, thus, crucial that the caller 
 *  does not access a value outside of a scope boundary. In other words, one must not use the value
 *  before calling {@code scope()} or after calling {@code descope()}, as the value may not be
 *  consistent with the current mapping at that point in time. The caller should discard
 *  references when leaving the scope, to avoid inadvertently accessing the values in a manner that
 *  violates safety.
 *
 *  @param <K> Key type.
 *  @param <V> Value type.
 */
public final class ReferenceCountingMap<K, V> {
  private static final int DEF_CAPACITY = 16;
  private static final float DEF_LOAD_FACTOR = 0.75f;
  private static final int DEF_CONCURRENCY_LEVEL = 16;
  
  private static final class Slot<V> {
    private final V value;
    
    private int uses = 1;

    Slot(V value) {
      this.value = value;
    }
  }
  
  private final Map<K, Slot<V>> map;
  
  /**
   *  Creates a reference-counting map with the default capacity, load factor and concurrency level.
   */
  public ReferenceCountingMap() {
    this(DEF_CAPACITY, DEF_LOAD_FACTOR, DEF_CONCURRENCY_LEVEL);
  }
  
  /**
   *  Creates a reference-counting map with the specified capacity, load factor and concurrency level. The
   *  interpretation of these values is equivalent to their namesakes in {@link ConcurrentHashMap}.
   *  
   *  @param initialCapacity The initial capacity.
   *  @param loadFactor The load factor.
   *  @param concurrencyLevel The concurrency level.
   */
  public ReferenceCountingMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
    map = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
  }
  
  /**
   *  Determines whether this map is empty.
   *  
   *  @return True if this map is empty.
   */
  public boolean isEmpty() {
    return map.isEmpty();
  }

  /**
   *  Obtains the present size of this map.
   *  
   *  @return The size of this map.
   */
  public int size() {
    return map.size();
  }
  
  /**
   *  Determines whether the map contains a mapping for the given key.
   *  
   *  @param key The key to query.
   *  @return True if a mapping is present.
   */
  public boolean containsKey(K key) {
    return map.containsKey(key);
  }
  
  /**
   *  Obtains an immutable set of keys contained in this map. <p>
   *  The set is backed by the map, so changes to the map are reflected in the set If the map is modified
   *  while an iteration over the set is in progress, the results of the iteration are undefined.
   *  
   *  @return An unmodifiable set of keys.
   */
  public Set<K> keySet() {
    return Collections.unmodifiableSet(map.keySet());
  }
  
  /**
   *  Attempts to atomically scope a reference to a value mapped by the given key, if such a mapping 
   *  is present. Failure will return a {@code null} without inserting the mapping.
   *  
   *  @param key The key.
   *  @return The value if one is present, or {@code null} otherwise.
   */
  public V tryScope(K key) {
    final Slot<V> slot = map.computeIfPresent(key, (__, existingSlot) -> {
      existingSlot.uses++;
      return existingSlot;
    });
    return slot != null ? slot.value : null;
  }
  
  /**
   *  Atomically scopes a reference to a value mapped by the given key, instantiating a value using
   *  the given {@code valueMaker} if no prior mapping exists.
   *  
   *  @param key The key.
   *  @param valueMaker A way of creating the value.
   *  @return The mapped value.
   */
  public V scope(K key, Supplier<? extends V> valueMaker) {
    return map.compute(key, (__, existingSlot) -> {
      if (existingSlot != null) {
        existingSlot.uses++;
        return existingSlot;
      } else {
        return new Slot<>(valueMaker.get());
      }
    }).value;
  }
  
  /**
   *  Atomically de-scopes the mapping for the given key, if one exists. Attempting to call
   *  {@link #descope(Object)} for a non-existent mapping will not have any further impact.
   *  
   *  @param key The key.
   */
  public void descope(K key) {
    map.computeIfPresent(key, (__, existingSlot) -> {
      existingSlot.uses--;
      return existingSlot.uses == 0 ? null : existingSlot;
    });
  }
  
  /**
   *  A scope-aware iterator of the mapped entries. The given {@code action} is invoked sequentially
   *  for each entry, acquiring a scope reference before calling the action, and de-scoping
   *  the entry afterwards.
   *  
   *  @param action The action to call for each entry.
   */
  public void forEach(BiConsumer<? super K, ? super V> action) {
    for (K key : map.keySet()) {
      final V value = tryScope(key);
      if (value != null) {
        try {
          action.accept(key, value);
        } finally {
          descope(key);
        }
      }
    }
  }
  
  @Override
  public String toString() {
    return ReferenceCountingMap.class.getSimpleName() + " [keySet=" + keySet() + "]";
  }
}
