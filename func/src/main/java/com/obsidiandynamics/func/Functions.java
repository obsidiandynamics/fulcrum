package com.obsidiandynamics.func;

import java.util.*;
import java.util.Map.*;
import java.util.function.*;

public final class Functions {
  private Functions() {}
  
  public static <U, V, X extends Throwable> CheckedSupplier<V, X> chain(CheckedSupplier<U, X> before, 
                                                                        CheckedFunction<U, V, X> after) throws X {
    return () -> after.apply(before.get());
  }

  public static <U, V, W, X extends Throwable> CheckedFunction<U, W, X> chain(CheckedFunction<U, V, X> before, 
                                                                              CheckedFunction<V, W, X> after) throws X {
    return u -> after.apply(before.apply(u));
  }

  public static <U, V, X extends Throwable> CheckedConsumer<U, X> chain(CheckedFunction<U, V, X> before, 
                                                                        CheckedConsumer<V, X> after) throws X {
    return u -> after.accept(before.apply(u));
  }

  public static <K, U, V, X extends Throwable> LinkedHashMap<K, V> 
      mapValues(Map<K, ? extends U> source, 
                CheckedFunction<? super U, ? extends V, X> converter) throws X {
    return mapValues(source, converter, LinkedHashMap::new);
  }

  public static <K, U, V, M extends Map<K, V>, X extends Throwable> M 
      mapValues(Map<K, ? extends U> source, 
                CheckedFunction<? super U, ? extends V, X> converter,
                Supplier<M> mapMaker) throws X {
    if (source != null) {
      final M mapped = mapMaker.get();
      for (Entry<K, ? extends U> entry : source.entrySet()) {
        mapped.put(entry.getKey(), converter.apply(entry.getValue()));
      }
      return mapped;
    } else {
      return null;
    }
  }

  public static <U, V, X extends Throwable> ArrayList<V> 
      mapCollection(Collection<? extends U> source, 
                    CheckedFunction<? super U, ? extends V, X> converter) throws X {
    return mapCollection(source, converter, ArrayList::new);
  }

  public static <U, V, C extends Collection<V>, X extends Throwable> C
      mapCollection(Collection<? extends U> source, 
                    CheckedFunction<? super U, ? extends V, X> converter,
                    Supplier<C> collectionMaker) throws X {
    if (source != null) {
      final C mapped = collectionMaker.get();
      for (U item : source) {
        mapped.add(converter.apply(item));
      }
      return mapped;
    } else {
      return null;
    }
  }

  public static <X extends Throwable> Supplier<X> withMessage(String message,
                                                              Function<String, X> exceptionMaker) {
    return () -> exceptionMaker.apply(message);
  }

  public static <X extends Throwable> Supplier<X> withMessage(Supplier<String> messageSupplier,
                                                              Function<String, X> exceptionMaker) {
    return () -> exceptionMaker.apply(messageSupplier.get());
  }

  public static <K, V, X extends Throwable> V mustExist(Map<K, V> map, 
                                                        K key, 
                                                        String errorTemplate, 
                                                        Function<String, X> exceptionMaker) throws X {
    return mustExist(map.get(key), withMessage(() -> String.format(errorTemplate, key), exceptionMaker));
  }

  public static <T, X extends Throwable> T mustExist(T value, Supplier<X> exceptionMaker) throws X {
    if (value != null) {
      return value;
    } else {
      throw exceptionMaker.get();
    }
  }

  public static <T, X extends Throwable> T mustBeSubtype(Object obj, Class<T> type, Supplier<X> exceptionMaker) throws X {
    if (type.isInstance(obj)) {
      return type.cast(obj);
    } else {
      throw exceptionMaker.get();
    }
  }

  public static <X extends Throwable> void mustBeEqual(Object expected, Object actual, Supplier<X> exceptionMaker) throws X {
    mustBeTrue(Objects.equals(expected, actual), exceptionMaker);
  }

  public static <X extends Throwable> void mustBeNull(Object obj, Supplier<X> exceptionMaker) throws X {
    mustBeTrue(obj == null, exceptionMaker);
  }

  public static <X extends Throwable> void mustBeTrue(boolean test, Supplier<X> exceptionMaker) throws X {
    if (! test) {
      throw exceptionMaker.get();
    }
  }

  public static <T, U, X extends Throwable> U ifPresentOptional(Optional<T> value,
                                                                CheckedFunction<? super T, ? extends U, X> mapper) throws X {
    return ifPresent(value.orElse(null), mapper);
  }
  
  public static <T, U, X extends Throwable> U ifPresent(T value, 
                                                        CheckedFunction<? super T, ? extends U, X> mapper) throws X {
    return value != null ? mapper.apply(value) : null;
  }

  public static <T, X extends Throwable> T ifAbsentOptional(Optional<T> value, 
                                                            CheckedSupplier<? extends T, X> supplier) throws X {
    return ifAbsent(value.orElse(null), supplier);
  }

  public static <T, X extends Throwable> T ifAbsent(T value, 
                                                    CheckedSupplier<? extends T, X> supplier) throws X {
    return value != null ? value : supplier.get();
  }

  public static <T, U, X extends Throwable> U ifEitherOptional(Optional<T> value, 
                                                               CheckedFunction<? super T, ? extends U, X> mapperIfPresent, 
                                                               CheckedSupplier<? extends U, X> supplierIfAbsent) throws X {
    return ifEither(value.orElse(null), mapperIfPresent, supplierIfAbsent);
  }

  public static <T, U, X extends Throwable> U ifEither(T value, 
                                                       CheckedFunction<? super T, ? extends U, X> mapperIfPresent, 
                                                       CheckedSupplier<? extends U, X> supplierIfAbsent) throws X {
    return value != null ? mapperIfPresent.apply(value) : supplierIfAbsent.get();
  }

  public static <T> T ifThrew(ThrowingSupplier<? extends T> supplier, Supplier<? extends T> supplierIfThrew) {
    try {
      return supplier.get();
    } catch (Throwable e) {
      return supplierIfThrew.get();
    }
  }

  public static <T> CheckedSupplier<T, RuntimeException> giveNull() {
    return () -> null;
  }

  public static <T> CheckedSupplier<T, RuntimeException> give(T value) {
    return () -> value;
  }

  public static <T> CheckedFunction<T, T, RuntimeException> identity() {
    return value -> value;
  }
}
