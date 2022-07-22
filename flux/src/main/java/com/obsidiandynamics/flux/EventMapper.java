package com.obsidiandynamics.flux;

import static com.obsidiandynamics.func.Functions.*;

import java.util.*;
import java.util.function.*;

import com.obsidiandynamics.threads.*;

@FunctionalInterface
public interface EventMapper<I, O> {
  void apply(EmissionContext<? super O> context, I input) throws InterruptedException;
  
  static <E> EventMapper<E, E> filter(Predicate<? super E> predicate) {
    return (context, input) -> {
      if (predicate.test(input)) context.emit(input);
    };
  }
  
  static <I, O> EventMapper<I, O> map(Function<? super I, ? extends O> mapper) {
    return (context, input) -> context.emit(mapper.apply(input));
  }
  
  static <I, O> EventMapper<I, O> flatMap(Function<? super I, Iterator<? extends O>> flatMapper) {
    return (context, input) -> {
      final Iterator<? extends O> iterator = flatMapper.apply(input);
      while (iterator.hasNext()) {
        context.emit(iterator.next());
      }
    };
  }
  
  static <E> EventMapper<E, E> sleep(long millis) {
    mustBeGreaterOrEqual(millis, 0, illegalArgument("Sleep time must be a non-negative number"));
    return (context, input) -> {
      Thread.sleep(millis);
      context.emit(input);
    };
  }
  
  static <E> EventMapper<E, E> sleep(long minMillis, long maxMillis) {
    mustBeGreaterOrEqual(minMillis, 0, illegalArgument("Minimum sleep time must be a non-negative number"));
    mustBeLessOrEqual(minMillis, maxMillis, 
                      illegalArgument("Minimum sleep time must not exceed the maximum sleep time"));
    return (context, input) -> {
      final long range = maxMillis - minMillis;
      final double randomRange = Math.random() * range;
      Threads.sleep(minMillis + (long) randomRange);
      context.emit(input);
    };
  }
  
  static <E> EventMapper<E, Timestamped<E>> timestamped() {
    return map(Timestamped::new);
  }
  
  static <E> EventMapper<E, E> skip(long count) {
    return new EventMapper<E, E>() {
      private long skipped;
      
      @Override
      public void apply(EmissionContext<? super E> context, E input) {
        if (skipped == count) {
          context.emit(input);
        } else {
          skipped++;
        }
      }
    };
  }
  
  static <E> EventMapper<E, E> take(long count) {
    return new EventMapper<E, E>() {
      private long took;
      
      @Override
      public void apply(EmissionContext<? super E> context, E input) {
        if (took < count) {
          took++;
          context.emit(input);
        } else {
          context.terminate();
        }
      }
    };
  }
  
  static <E> EventMapper<E, E> distinct(Function<? super E, ?> extractor) {
    return new EventMapper<E, E>() {
      private final Set<Object> forwarded = new HashSet<>();
      
      @Override
      public void apply(EmissionContext<? super E> context, E input) {
        final Object extracted = extractor.apply(input);
        if (forwarded.add(extracted)) {
          context.emit(input);
        }
      }
    };
  }
}
