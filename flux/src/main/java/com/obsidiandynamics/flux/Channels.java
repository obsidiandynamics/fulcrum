package com.obsidiandynamics.flux;

import java.util.*;
import java.util.function.*;

public final class Channels {
  private Channels() {}
  
  public static <E> BufferedChannel<E> buffered() {
    return buffered(Integer.MAX_VALUE);
  }
  
  public static <E> BufferedChannel<E> buffered(BackingQueueFactory queueFactory) {
    return buffered(Integer.MAX_VALUE, queueFactory);
  }
  
  public static <E> BufferedChannel<E> buffered(int capacity) {
    return buffered(capacity, HeapBackingQueueFactory.getInstance());
  }
  
  public static <E> BufferedChannel<E> buffered(int capacity, BackingQueueFactory queueFactory) {
    return new BufferedChannel<>(queueFactory, capacity);
  }
  
  public static <I, O> MappingChannel<I, O> map(EventMapper<? super I, ? extends O> eventMapper) {
    return new MappingChannel<>(eventMapper);
  }
  
  public static <E> MappingChannel<E, E> filter(Predicate<? super E> predicate) {
    return map(EventMapper.filter(predicate));
  }
  
  public static <I, O> MappingChannel<I, O> map(Function<? super I, ? extends O> mapper) {
    return map(EventMapper.map(mapper));
  }
  
  public static <I, O> MappingChannel<I, O> flatMap(Function<? super I, Iterator<? extends O>> flatMapper) {
    return map(EventMapper.flatMap(flatMapper));
  }
  
  public static <E> MappingChannel<E, E> sleep(long millis) {
    return map(EventMapper.sleep(millis));
  }
  
  public static <E> MappingChannel<E, E> sleep(long minMillis, long maxMillis) {
    return map(EventMapper.sleep(minMillis, maxMillis));
  }
  
  public static <E> MappingChannel<E, Timestamped<E>> timestamped() {
    return map(EventMapper.timestamped());
  }
  
  public static <E> MappingChannel<E, E> skip(long count) {
    return map(EventMapper.skip(count));
  }
  
  public static <E> MappingChannel<E, E> take(long count) {
    return map(EventMapper.take(count));
  }
  
  public static <E> MappingChannel<E, E> distinct() {
    return map(EventMapper.distinct(Function.identity()));
  }
  
  public static <E> MappingChannel<E, E> distinct(Function<? super E, ?> extractor) {
    return map(EventMapper.distinct(extractor));
  }
}
