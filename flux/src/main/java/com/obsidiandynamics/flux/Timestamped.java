package com.obsidiandynamics.flux;

import java.util.*;
import java.util.function.*;

/**
 *  Augments a typed value with a nanosecond-precision timestamp.
 *
 *  @param <T> Encapsulated type.
 */
public final class Timestamped<T> {
  /** Timestamp of event creation (in nanos). */
  private final long timestamp;
  
  private final T value;

  public Timestamped(T value) {
    this(System.nanoTime(), value);
  }

  public Timestamped(long timestamp, T value) {
    this.timestamp = timestamp;
    this.value = value;
  }
  
  public long getTimestamp() {
    return timestamp;
  }
  
  public T getValue() {
    return value;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Long.hashCode(timestamp);
    result = prime * result + Objects.hashCode(value);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof Timestamped) {
      final Timestamped<?> that = (Timestamped<?>) obj;
      return timestamp == that.timestamp && Objects.equals(value, that.value);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return Timestamped.class.getSimpleName() + " [timestamp=" + timestamp + ", value=" + value + "]";
  }
  
  /**
   *  Maps one {@link Timestamped} instance to another, where
   *  the transformation is applied to the payload, while preserving the original timestamp.
   *  
   *  @param <I> Input value type.
   *  @param <O> Output value type.
   *  @param mapper The value mapper function.
   *  @return A higher level function that operates at {@link Timestamped} level.
   */
  public static <I, O> Function<Timestamped<I>, Timestamped<O>> mapPreserve(Function<I, O> mapper) {
    return input -> new Timestamped<>(input.timestamp, mapper.apply(input.getValue()));
  }
  
  /**
   *  Maps one {@link Timestamped} instance to another, where
   *  the transformation is applied to the payload, while the timestamp is updated to reflect
   *  the time of the transformation.
   *  
   *  @param <I> Input value type.
   *  @param <O> Output value type.
   *  @param mapper The value mapper function.
   *  @return A higher level function that operates at {@link Timestamped} level.
   */
  public static <I, O> Function<Timestamped<I>, Timestamped<O>> mapRestamp(Function<I, O> mapper) {
    return input -> new Timestamped<>(mapper.apply(input.getValue()));
  }
}
