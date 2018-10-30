package com.obsidiandynamics.func;

import java.util.*;

/**
 *  Fluent builder of a {@link Map}.
 *  
 *  @param <K> Key type.
 *  @param <V> Value type.
 */
public final class MapBuilder<K, V> {
  private final Map<K, V> map;
  
  public MapBuilder() {
    this(new LinkedHashMap<>());
  }

  public MapBuilder(Map<K, V> map) {
    this.map = map;
  }
  
  public MapBuilder<K, V> with(K key, V value) {
    map.put(key, value);
    return this;
  }
  
  public Map<K, V> build() {
    return map;
  }
  
  @Override
  public String toString() {
    return MapBuilder.class.getSimpleName() + " [map=" + map + "]";
  }
  
  public static <K, V> MapBuilder<K, V> init(K key, V value) {
    return new MapBuilder<K, V>().with(key, value);
  }
}
