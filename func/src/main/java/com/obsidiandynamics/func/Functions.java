package com.obsidiandynamics.func;

import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;

/**
 *  Helper methods assisting with a range of FP (functional programming) patterns, 
 *  particularly (although not only) scenarios that rely on functions that throw checked 
 *  exceptions.
 */
public final class Functions {
  private Functions() {}
  
  /**
   *  Applies a function to the result of a supplier, thereby creating a new supplier.
   *  
   *  @param <U> Original type supplied by {@code before} and input to the {@code after} function.
   *  @param <V> Output type of the {@code after} function.
   *  @param <X> Exception type.
   *  @param before The original value supplier.
   *  @param after The transformation to apply to the supplied value.
   *  @return A composite {@link CheckedSupplier}.
   *  @throws X If an error occurs.
   */
  public static <U, V, X extends Throwable> CheckedSupplier<V, X> chain(CheckedSupplier<U, ? extends X> before, 
                                                                        CheckedFunction<U, V, ? extends X> after) throws X {
    return () -> after.apply(before.get());
  }

  /**
   *  Chains the output of one function into the input of another, thereby creating a new
   *  function.
   *  
   *  @see CheckedFunction#andThen(CheckedFunction)
   *  
   *  @param <U> Initial input type of the {@code before} function.
   *  @param <V> Output type of the {@code before} function and the input of {@code after}.
   *  @param <W> Output type of the {@code after} function.
   *  @param <X> Exception type.
   *  @param before The initial function.
   *  @param after The function to chain to.
   *  @return A composite {@link CheckedFunction}.
   *  @throws X If an error occurs.
   */
  public static <U, V, W, X extends Throwable> CheckedFunction<U, W, X> chain(CheckedFunction<U, V, X> before, 
                                                                              CheckedFunction<V, W, X> after) throws X {
    return before.andThen(after);
  }

  /**
   *  Chains the output of a function into the input of a consumer, thereby creating a new
   *  consumer.
   *  
   *  @param <U> Input type of the {@code before} function.
   *  @param <V> Output type of the {@code before} function and input type of the {@code after} consumer.
   *  @param <X> Exception type.
   *  @param before The initial function to apply.
   *  @param after The consumer to chain to.
   *  @return A composite {@link CheckedConsumer}.
   *  @throws X If an error occurs.
   */
  public static <U, V, X extends Throwable> CheckedConsumer<U, X> chain(CheckedFunction<U, V, X> before, 
                                                                        CheckedConsumer<V, X> after) throws X {
    return u -> after.accept(before.apply(u));
  }

  /**
   *  Maps values from the {@code source} map to a new {@link LinkedHashMap} instance, 
   *  using the given mapping function. The mapping function only 
   *  operates on the values; the keys are copied as-is.
   *  
   *  @param <K> Key type.
   *  @param <U> Source value type.
   *  @param <V> Target value type.
   *  @param <X> Exception type.
   *  @param source The source map.
   *  @param mapper The mapping function applied to values.
   *  @return The target {@link LinkedHashMap}.
   *  @throws X If an error occurs within the mapping function.
   */
  public static <K, U, V, X extends Throwable> LinkedHashMap<K, V> 
      mapValues(Map<K, ? extends U> source, 
                CheckedFunction<? super U, ? extends V, ? extends X> mapper) throws X {
    return mapValues(source, mapper, LinkedHashMap::new);
  }

  /**
   *  Maps values from the {@code source} map to a new map instance, using the given
   *  mapping function. The mapping function only works on the values;
   *  the keys are copied as-is.
   *  
   *  @param <K> Key type.
   *  @param <U> Source value type.
   *  @param <V> Target value type.
   *  @param <M> Target map type.
   *  @param <X> Exception type.
   *  @param source The source map.
   *  @param mapper The mapping function applied to values.
   *  @param mapMaker A way of creating a new map instance to store the mapped entries.
   *  @return The target map.
   *  @throws X If an error occurs within the mapping function.
   */
  public static <K, U, V, M extends Map<K, V>, X extends Throwable> M 
      mapValues(Map<K, ? extends U> source, 
                CheckedFunction<? super U, ? extends V, ? extends X> mapper,
                Supplier<M> mapMaker) throws X {
    if (source != null) {
      final M mapped = mapMaker.get();
      for (Entry<K, ? extends U> entry : source.entrySet()) {
        mapped.put(entry.getKey(), mapper.apply(entry.getValue()));
      }
      return mapped;
    } else {
      return null;
    }
  }

  /**
   *  Maps elements from the {@code source} collection to a new {@link ArrayList} instance,
   *  using the given mapping function.
   *  
   *  @param <U> Source element type.
   *  @param <V> Target element type.
   *  @param <X> Exception type.
   *  @param source The source collection.
   *  @param mapper The mapping function applied to elements.
   *  @return The target {@link ArrayList}.
   *  @throws X If an error occurs within the mapping function.
   */
  public static <U, V, X extends Throwable> ArrayList<V> 
      mapCollection(Collection<? extends U> source, 
                    CheckedFunction<? super U, ? extends V, ? extends X> mapper) throws X {
    return mapCollection(source, mapper, ArrayList::new);
  }

  /**
   *  Maps elements from the {@code source} collection to a new collection instance,
   *  using the given mapping function.
   *  
   *  @param <U> Source element type.
   *  @param <V> Target element type.
   *  @param <C> Target collection type.
   *  @param <X> Exception type.
   *  @param source The source collection.
   *  @param mapper The mapping function applied to elements.
   *  @param collectionMaker A way of creating a new collection to store the mapped elements.
   *  @return The target collection.
   *  @throws X If an error occurs within the mapping function.
   */
  public static <U, V, C extends Collection<V>, X extends Throwable> C
      mapCollection(Collection<? extends U> source, 
                    CheckedFunction<? super U, ? extends V, ? extends X> mapper,
                    Supplier<C> collectionMaker) throws X {
    if (source != null) {
      final C mapped = collectionMaker.get();
      for (U item : source) {
        mapped.add(mapper.apply(item));
      }
      return mapped;
    } else {
      return null;
    }
  }

  /**
   *  Collects elements from the {@code source} stream in parallel, having applied a mapping function,
   *  storing the result in a new {@link ArrayList}. <p>
   *  
   *  This method is semantically analogous to the chaining of {@link Stream#map(Function)} with
   *  {@link Stream#collect(Collector)}, having first invoked {@link Stream#parallel()},
   *  with two notable differences: <br>
   *  - This method accepts a {@link CheckedFunction} to perform the mapping, which permits
   *    checked exceptions; and <br>
   *  - This method allows the caller to nominate a custom {@link ExecutorService}, which is used
   *    to drive the mapping in parallel.
   *  
   *  @param <U> Source element type.
   *  @param <V> Target element type.
   *  @param <X> Exception type.
   *  @param source The source stream.
   *  @param mapper The mapping function applied to the elements.
   *  @param executor The executor to use.
   *  @return The mapped {@link ArrayList}.
   *  @throws X If an error occurs within the mapping function.
   *  @throws InterruptedException If the calling thread was interrupted while waiting for the result
   *                               of the parallel jobs.
   */
  public static <U, V, X extends Exception> ArrayList<V>
      parallelMapStream(Stream<? extends U> source, 
                        CheckedFunction<? super U, ? extends V, ? extends X> mapper,
                        ExecutorService executor) throws X, InterruptedException {
    return parallelMapStream(source, mapper, ArrayList::new, executor);
  }

  /**
   *  Collects elements from the {@code source} stream in parallel, having applied a mapping function,
   *  to a new collection constructed by the given {@code collectionMaker}. <p>
   *  
   *  This method is semantically analogous to the chaining of {@link Stream#map(Function)} with
   *  {@link Stream#collect(Collector)}, having first invoked {@link Stream#parallel()},
   *  with two notable differences: <br>
   *  - This method accepts a {@link CheckedFunction} to perform the mapping, which permits
   *    checked exceptions; and <br>
   *  - This method allows the caller to nominate a custom {@link ExecutorService}, which is used
   *    to drive the mapping in parallel.
   *  
   *  @param <U> Source element type.
   *  @param <V> Target element type.
   *  @param <C> Target collection type.
   *  @param <X> Exception type.
   *  @param source The source stream.
   *  @param mapper The mapping function applied to the elements.
   *  @param collectionMaker A way of creating the target collection.
   *  @param executor The executor to use.
   *  @return The mapped collection.
   *  @throws X If an error occurs within the mapping function.
   *  @throws InterruptedException If the calling thread was interrupted while waiting for the result
   *                               of the parallel jobs.
   */
  public static <U, V, C extends Collection<V>, X extends Throwable> C
      parallelMapStream(Stream<? extends U> source, 
                        CheckedFunction<? super U, ? extends V, ? extends X> mapper,
                        Supplier<C> collectionMaker,
                        ExecutorService executor) throws X, InterruptedException {
    final List<Future<V>> submissions = new ArrayList<>();
    source.forEach(item -> {
      submissions.add(executor.submit(() -> Exceptions.wrap(() -> mapper.apply(item), CapturedException::new)));
    });
    
    final C mapped = collectionMaker.get();
    for (Future<V> future : submissions) {
      try {
        mapped.add(future.get());
      } catch (ExecutionException e) {
        final Throwable cause = CapturedException.unwind(e.getCause());
        throw Classes.<X>cast(cause);
      }
    }
    return mapped;
  }
  
  /**
   *  Submits each element of the given collection to a checked consumer. An exception (if any)
   *  thrown within the consumer is propagated to the caller.
   *  
   *  @param <T> Element type.
   *  @param <X> Exception type.
   *  @param collection The collection to traverse.
   *  @param consumer The consumer.
   *  @throws X If an exception occurs.
   */
  public static <T, X extends Throwable> void forEach(Collection<? extends T> collection, 
                                                      CheckedConsumer<? super T, ? extends X> consumer) throws X {
    for (T element : collection) {
      consumer.accept(element);
    }
  }
  
  /**
   *  Used to capture an exception thrown within a {@link Callable} submitted to an {@link ExecutorService}. <p>
   *  
   *  Some executors, such as a {@link ForkJoinPool}, will wrap checked exceptions thrown from a {@link Callable}
   *  inside a {@link RuntimeException}, making it impossible to reliably determine the true exception thrown
   *  by walking the cause chain. (We can't tell whether an observed {@link RuntimeException} was thrown from
   *  the application code or was introduced by a {@link ForkJoinTask}.) Using a private exception wrapper
   *  lets us discard any exceptions introduced by an {@link ExecutorService}.
   */
  static final class CapturedException extends Exception {
    private static final long serialVersionUID = 1L;
    
    CapturedException(Throwable cause) { super(cause); }
    
    static Throwable unwind(Throwable throwable) {
      for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
        if (cause instanceof CapturedException) {
          return cause.getCause();
        }
      }
      throw new IllegalStateException("Could not unwind the correct cause", throwable);
    }
  }
  
  /**
   *  A variant of {@link #withMessage(String, Function)} that produces an
   *  {@link IllegalStateException}.
   *  
   *  @param message The exception message.
   *  @return The exception {@code Supplier}.
   */
  public static Supplier<IllegalStateException> illegalState(String message) {
    return withMessage(message, IllegalStateException::new);
  }
  
  /**
   *  A variant of {@link #withMessage(String, Function)} that produces an
   *  {@link IllegalArgumentException}.
   *  
   *  @param message The exception message.
   *  @return The exception {@code Supplier}.
   */
  public static Supplier<IllegalArgumentException> illegalArgument(String message) {
    return withMessage(message, IllegalArgumentException::new);
  }

  /**
   *  Creates a supplier of a custom {@link Throwable} that is constructed using a given {@code message}.
   *  
   *  @param <X> Exception type.
   *  @param message The exception message.
   *  @param exceptionMaker A way of creating a {@link Throwable} that uses this message.
   *  @return The exception {@code Supplier}.
   */
  public static <X extends Throwable> Supplier<X> withMessage(String message,
                                                              Function<String, ? extends X> exceptionMaker) {
    return () -> exceptionMaker.apply(message);
  }

  /**
   *  Creates a supplier of a custom {@link Throwable} that is constructed using a given
   *  (lazily evaluated) message supplier.
   *  
   *  @param <X> Exception type.
   *  @param messageSupplier Supplies the exception message when invoked.
   *  @param exceptionMaker A way of creating a {@link Throwable} that uses this message.
   *  @return The exception {@code Supplier}.
   */
  public static <X extends Throwable> Supplier<X> withMessage(Supplier<String> messageSupplier,
                                                              Function<String, ? extends X> exceptionMaker) {
    return () -> exceptionMaker.apply(messageSupplier.get());
  }

  /**
   *  Ensures that the given {@code map} contains the specified {@code key} and returns the
   *  mapped value. Otherwise, an exception specified by the given {@code exceptionMaker} is
   *  thrown.
   *  
   *  @param <K> Key type.
   *  @param <V> Value type.
   *  @param <X> Exception type.
   *  @param map The map to query.
   *  @param key The key that must be present.
   *  @param errorTemplate The template for forming the error, where the single format specifier
   *                       '%s' is substituted for the missing key.
   *  @param exceptionMaker A way of creating the exception for a missing value.
   *  @return The value.
   *  @throws X If the mapping wasn't present.
   */
  public static <K, V, X extends Throwable> V mustExist(Map<K, V> map, 
                                                        K key, 
                                                        String errorTemplate, 
                                                        Function<String, ? extends X> exceptionMaker) throws X {
    return mustExist(map.get(key), withMessage(() -> String.format(errorTemplate, key), exceptionMaker));
  }

  /**
   *  A convenient variant of {@link #mustExist(Object, Supplier)} that throws a
   *  {@link NullArgumentException} with no message.
   *  
   *  @param <T> Value type.
   *  @param value The value to check.
   *  @return The verified value.
   *  @throws NullArgumentException If the given value is {@code null}.
   */
  public static <T> T mustExist(T value) throws NullArgumentException {
    return mustExist(value, NullArgumentException::new);
  }

  /**
   *  A convenient variant of {@link #mustExist(Object, Supplier)} that throws a
   *  {@link NullArgumentException} with the supplied {@code message}.
   *  
   *  @param <T> Value type.
   *  @param value The value to check.
   *  @param message The message to the {@link NullArgumentException}.
   *  @return The verified value.
   *  @throws NullArgumentException If the given value is {@code null}.
   */
  public static <T> T mustExist(T value, String message) throws NullArgumentException {
    return mustExist(value, withMessage(message, NullArgumentException::new));
  }

  /**
   *  Ensures that the given value must exist (i.e. it cannot be {@code null}). Otherwise, an
   *  exception specified by the given {@code exceptionMaker} is thrown.
   *  
   *  @param <T> Value type.
   *  @param <X> Exception type.
   *  @param value The value to check.
   *  @param exceptionMaker A way of creating the exception for a {@code null} value.
   *  @return The verified value.
   *  @throws X If the given value is {@code null}.
   */
  public static <T, X extends Throwable> T mustExist(T value, Supplier<? extends X> exceptionMaker) throws X {
    if (value != null) {
      return value;
    } else {
      throw exceptionMaker.get();
    }
  }

  /**
   *  Ensures that the given value is {@code null}. Otherwise, an
   *  exception specified by the given {@code exceptionMaker} is thrown.
   *  
   *  @param <X> Exception type.
   *  @param value The value to test.
   *  @param exceptionMaker A way of creating the exception for a non-{@code null} value.
   *  @throws X If the value is not {@code null}.
   */
  public static <X extends Throwable> void mustBeNull(Object value, Supplier<? extends X> exceptionMaker) throws X {
    mustBeTrue(value == null, exceptionMaker);
  }

  /**
   *  Ensures that the given object is a subclass of the specified class type. Otherwise, an
   *  exception specified by the given {@code exceptionMaker} is thrown.
   *  
   *  @param <T> Object type.
   *  @param <X> Exception type.
   *  @param value The object to verify.
   *  @param type The class type that the object must be a type of.
   *  @param exceptionMaker A way of creating the exception for a non-matching object.
   *  @return The verified value.
   *  @throws X If the given object is not of a matching type.
   */
  public static <T, X extends Throwable> T mustBeSubtype(Object value, Class<T> type, Supplier<? extends X> exceptionMaker) throws X {
    if (type.isInstance(value)) {
      return type.cast(value);
    } else {
      throw exceptionMaker.get();
    }
  }

  /**
   *  Ensures that the given {@code expected} and {@code actual} values are equal. Otherwise, an
   *  exception specified by the given {@code exceptionMaker} is thrown.
   *  
   *  @param <X> Exception type.
   *  @param expected The expected value.
   *  @param actual The actual value.
   *  @param exceptionMaker A way of creating the exception for non-equal objects.
   *  @throws X If the two objects are not equal.
   */
  public static <X extends Throwable> void mustBeEqual(Object expected, Object actual, Supplier<? extends X> exceptionMaker) throws X {
    mustBeTrue(Objects.equals(expected, actual), exceptionMaker);
  }

  /**
   *  Ensures that the given {@code unexpected} and {@code actual} values are not equal. Otherwise, an
   *  exception specified by the given {@code exceptionMaker} is thrown.
   *  
   *  @param <T> Value type.
   *  @param <X> Exception type.
   *  @param unexpected The unexpected value.
   *  @param actual The actual value.
   *  @param exceptionMaker A way of creating the exception for equal objects.
   *  @return The verified value.
   *  @throws X If the two objects are equal.
   */
  public static <T, X extends Throwable> T mustNotBeEqual(Object unexpected, T actual, Supplier<? extends X> exceptionMaker) throws X {
    mustBeFalse(Objects.equals(unexpected, actual), exceptionMaker);
    return actual;
  }

  /**
   *  Ensures that the given Boolean {@code test} value is {@code true}. Otherwise, an
   *  exception specified by the given {@code exceptionMaker} is thrown.
   *  
   *  @param <X> Exception type.
   *  @param test The Boolean value to test.
   *  @param exceptionMaker A way of creating the exception for a {@code false} value.
   *  @throws X If the value is {@code false}.
   */
  public static <X extends Throwable> void mustBeTrue(boolean test, Supplier<? extends X> exceptionMaker) throws X {
    if (! test) {
      throw exceptionMaker.get();
    }
  }

  /**
   *  Ensures that the given Boolean {@code test} value is {@code false}. Otherwise, an
   *  exception specified by the given {@code exceptionMaker} is thrown.
   *  
   *  @param <X> Exception type.
   *  @param test The Boolean value to test.
   *  @param exceptionMaker A way of creating the exception for a {@code true} value.
   *  @throws X If the value is {@code true}.
   */
  public static <X extends Throwable> void mustBeFalse(boolean test, Supplier<? extends X> exceptionMaker) throws X {
    if (test) {
      throw exceptionMaker.get();
    }
  }
  
  /**
   *  Ensures that {@code value} is greater than {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <X extends Throwable> long mustBeGreater(long value, long comparand, Supplier<? extends X> exceptionMaker) throws X {
    if (value > comparand) return value; else throw exceptionMaker.get();
  }
  
  /**
   *  Ensures that {@code value} is greater than or equal to {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <X extends Throwable> long mustBeGreaterOrEqual(long value, long comparand, Supplier<? extends X> exceptionMaker) throws X {
    if (value >= comparand) return value; else throw exceptionMaker.get();
  }
  
  /**
   *  Ensures that {@code value} is less than {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <X extends Throwable> long mustBeLess(long value, long comparand, Supplier<? extends X> exceptionMaker) throws X {
    if (value < comparand) return value; else throw exceptionMaker.get();
  }
  
  /**
   *  Ensures that {@code value} is less than or equal to {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <X extends Throwable> long mustBeLessOrEqual(long value, long comparand, Supplier<? extends X> exceptionMaker) throws X {
    if (value <= comparand) return value; else throw exceptionMaker.get();
  }
  
  /**
   *  Ensures that {@code value} is greater than {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <X extends Throwable> int mustBeGreater(int value, int comparand, Supplier<? extends X> exceptionMaker) throws X {
    if (value > comparand) return value; else throw exceptionMaker.get();
  }
  
  /**
   *  Ensures that {@code value} is greater than or equal to {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <X extends Throwable> int mustBeGreaterOrEqual(int value, int comparand, Supplier<? extends X> exceptionMaker) throws X {
    if (value >= comparand) return value; else throw exceptionMaker.get();
  }

  /**
   *  Ensures that {@code value} is less than {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <X extends Throwable> int mustBeLess(int value, int comparand, Supplier<? extends X> exceptionMaker) throws X {
    if (value < comparand) return value; else throw exceptionMaker.get();
  }

  /**
   *  Ensures that {@code value} is less than or equal to {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <X extends Throwable> int mustBeLessOrEqual(int value, int comparand, Supplier<? extends X> exceptionMaker) throws X {
    if (value <= comparand) return value; else throw exceptionMaker.get();
  }
  
  /**
   *  Ensures that {@code value} is greater than {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <X extends Throwable> double mustBeGreater(double value, double comparand, Supplier<? extends X> exceptionMaker) throws X {
    if (value > comparand) return value; else throw exceptionMaker.get();
  }
  
  /**
   *  Ensures that {@code value} is greater than or equal to {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <X extends Throwable> double mustBeGreaterOrEqual(double value, double comparand, Supplier<? extends X> exceptionMaker) throws X {
    if (value >= comparand) return value; else throw exceptionMaker.get();
  }
  
  /**
   *  Ensures that {@code value} is less than {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <X extends Throwable> double mustBeLess(double value, double comparand, Supplier<? extends X> exceptionMaker) throws X {
    if (value < comparand) return value; else throw exceptionMaker.get();
  }
  
  /**
   *  Ensures that {@code value} is less than or equal to {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <X extends Throwable> double mustBeLessOrEqual(double value, double comparand, Supplier<? extends X> exceptionMaker) throws X {
    if (value <= comparand) return value; else throw exceptionMaker.get();
  }
  
  /**
   *  Ensures that {@code value} is greater than {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <X extends Throwable> float mustBeGreater(float value, float comparand, Supplier<? extends X> exceptionMaker) throws X {
    if (value > comparand) return value; else throw exceptionMaker.get();
  }
  
  /**
   *  Ensures that {@code value} is greater than or equal to {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <X extends Throwable> float mustBeGreaterOrEqual(float value, float comparand, Supplier<? extends X> exceptionMaker) throws X {
    if (value >= comparand) return value; else throw exceptionMaker.get();
  }
  
  /**
   *  Ensures that {@code value} is less than {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <X extends Throwable> float mustBeLess(float value, float comparand, Supplier<? extends X> exceptionMaker) throws X {
    if (value < comparand) return value; else throw exceptionMaker.get();
  }
  
  /**
   *  Ensures that {@code value} is less than or equal to {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <X extends Throwable> float mustBeLessOrEqual(float value, float comparand, Supplier<? extends X> exceptionMaker) throws X {
    if (value <= comparand) return value; else throw exceptionMaker.get();
  }
  
  /**
   *  Ensures that {@code value} is greater than {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <T> Value type.
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <T extends Comparable<T>, X extends Throwable> T mustBeGreater(T value, T comparand, Supplier<? extends X> exceptionMaker) throws X {
    mustExist(value);
    mustExist(comparand);
    if (value.compareTo(comparand) > 0) return value; else throw exceptionMaker.get();
  }
  
  /**
   *  Ensures that {@code value} is greater than or equal to {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <T> Value type.
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <T extends Comparable<T>, X extends Throwable> T mustBeGreaterOrEqual(T value, T comparand, Supplier<? extends X> exceptionMaker) throws X {
    mustExist(value);
    mustExist(comparand);
    if (value.compareTo(comparand) >= 0) return value; else throw exceptionMaker.get();
  }
  
  /**
   *  Ensures that {@code value} is less than {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <T> Value type.
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <T extends Comparable<T>, X extends Throwable> T mustBeLess(T value, T comparand, Supplier<? extends X> exceptionMaker) throws X {
    mustExist(value);
    mustExist(comparand);
    if (value.compareTo(comparand) < 0) return value; else throw exceptionMaker.get();
  }
  
  /**
   *  Ensures that {@code value} is less than or equal to {@code comparand}, returning the verified value if 
   *  true or throwing an exception otherwise.
   *  
   *  @param <T> Value type.
   *  @param <X> Exception type.
   *  @param value The value to compare.
   *  @param comparand The comparand.
   *  @param exceptionMaker A way of creating the exception for a failed comparison.
   *  @return The verified value, as supplied to this method.
   *  @throws X If the comparison failed.
   */
  public static <T extends Comparable<T>, X extends Throwable> T mustBeLessOrEqual(T value, T comparand, Supplier<? extends X> exceptionMaker) throws X {
    mustExist(value);
    mustExist(comparand);
    if (value.compareTo(comparand) <= 0) return value; else throw exceptionMaker.get();
  }

  /**
   *  A variant of {@link #ifPresent(Object, CheckedFunction)} that operates on {@link Optional} values,
   *  returning the result of applying the given {@code mapper} to the encapsulated value if the
   *  latter is present, or {@code null} otherwise. <p>
   *  
   *  This is similar to {@link Optional#map(Function)}, with two notable differences: <br>
   *  - This method does not create intermediate objects; and <br>
   *  - This method accepts a {@link CheckedFunction}, permitting exceptions.
   *  
   *  @param <T> Value type.
   *  @param <U> Mapped type.
   *  @param <X> Exception type.  
   *  @param value The optional value.
   *  @param mapper The mapping function.
   *  @return The mapped value if the initial value isn't empty, or {@code null} otherwise.
   *  @throws X If an error occurs.
   */
  public static <T, U, X extends Throwable> U ifPresentOptional(Optional<T> value,
                                                                CheckedFunction<? super T, ? extends U, X> mapper) throws X {
    return ifPresent(value.orElse(null), mapper);
  }
  
  /**
   *  Evaluates a given {@code mapper} function, passing it the given {@code value} if the latter
   *  is non-{@code null}, returning the mapped value. Otherwise, if the given {@code value} is
   *  {@code null}, a {@code null} is returned. This is the functional equivalent of the
   *  Elvis operator. <p>
   *  
   *  Note: as the mapping function is free to output {@code null}, this method can return a
   *  {@code null} for a non-{@code null} input value.
   *  
   *  @param <T> Value type.
   *  @param <U> Mapped type.
   *  @param <X> Exception type.
   *  @param value The value to map, if it isn't {@code null}.
   *  @param mapper The mapping function to invoke if the value isn't {@code null}.
   *  @return The mapped value if the initial value isn't {@code null}, or {@code null} otherwise.
   *  @throws X If an error occurs inside the mapping function.
   */
  public static <T, U, X extends Throwable> U ifPresent(T value, 
                                                        CheckedFunction<? super T, ? extends U, X> mapper) throws X {
    return value != null ? mapper.apply(value) : null;
  }

  /**
   *  Invokes a given {@link CheckedConsumer}, passing it the given {@code value} if and only if the latter
   *  is non-{@code null}.
   *  
   *  @param <T> Value type.
   *  @param <X> Exception type.
   *  @param value The value to consume, if it isn't {@code null}.
   *  @param consumer The consumer to invoke.
   *  @throws X If an error occurs inside the consumer.
   */
  public static <T, X extends Throwable> void ifPresentVoid(T value, 
                                                            CheckedConsumer<? super T, X> consumer) throws X {
    if (value != null) consumer.accept(value);
  }

  /**
   *  A variant of {@link #ifAbsent(Object, CheckedSupplier)} that operates on {@link Optional} values,
   *  returning the encapsulated value if set, or sourcing the value from the given {@code supplier}
   *  otherwise. <p>
   *  
   *  This is similar to {@link Optional#orElseGet(Supplier)}, with two notable differences: <br>
   *  - This method does not create intermediate objects; and <br>
   *  - This method accepts a {@link CheckedSupplier}, permitting exceptions.
   *  
   *  @param <T> Value type.
   *  @param <X> Exception type.
   *  @param value The optional value.
   *  @param supplier Supplier to invoke if the value is empty.
   *  @return The extracted value (as-is) if it is set, or the value produced by the supplier otherwise.
   *  @throws X If an error occurs inside the supplier.
   */
  public static <T, X extends Throwable> T ifAbsentOptional(Optional<T> value, 
                                                            CheckedSupplier<? extends T, X> supplier) throws X {
    return ifAbsent(value.orElse(null), supplier);
  }

  /**
   *  Returns the given {@code value} if it is non-{@code null}, otherwise a value produced
   *  by the given {@code supplier} is returned instead. <p>
   *  
   *  Note: as the supplier is free to output {@code null}, this method can return a
   *  {@code null} for a {@code null} input value.
   *  
   *  @param <T> Value type.
   *  @param <X> Exception type.
   *  @param value The value to consider.
   *  @param supplier Supplier to invoke if the given value is {@code null}.
   *  @return The given value (as-is) if it is non-{@code null}, or the value produced by the supplier otherwise.
   *  @throws X If an error occurs inside the supplier.
   */
  public static <T, X extends Throwable> T ifAbsent(T value, 
                                                    CheckedSupplier<? extends T, X> supplier) throws X {
    return value != null ? value : supplier.get();
  }

  /**
   *  A variant of {@link #ifEither(Object, CheckedFunction, CheckedSupplier)} that operates on
   *  {@link Optional} values.
   *  
   *  @param <T> Value type.
   *  @param <U> Mapped or supplied type.
   *  @param <X> Exception type.
   *  @param value The optional value.
   *  @param mapperIfPresent The mapping function to invoke if the value is set.
   *  @param supplierIfAbsent The supplier to invoke if the value is empty.
   *  @return The mapped value if the initial value isn't empty, or the value produced by
   *          the supplier otherwise.
   *  @throws X If an error occurs inside either the mapping function or the supplier.
   */
  public static <T, U, X extends Throwable> U ifEitherOptional(Optional<T> value, 
                                                               CheckedFunction<? super T, ? extends U, X> mapperIfPresent, 
                                                               CheckedSupplier<? extends U, X> supplierIfAbsent) throws X {
    return ifEither(value.orElse(null), mapperIfPresent, supplierIfAbsent);
  }

  /**
   *  A hybrid of {@link #ifPresent(Object, CheckedFunction)} and {@link #ifAbsent(Object, CheckedSupplier)},
   *  returning the application of the {@code mapperIfPresent} function to the given {@code value} if it 
   *  isn't {@code null}, or sourcing the value from the given {@code supplierIfAbsent} supplier if the 
   *  value is {@code null}.
   *  
   *  @param <T> Value type.
   *  @param <U> Mapped or supplied type.
   *  @param <X> Exception type.
   *  @param value The value to consider.
   *  @param mapperIfPresent The mapping function to invoke if the value is non-{@code null}.
   *  @param supplierIfAbsent The supplier to invoke if the value is {@code null}.
   *  @return The mapped value if the initial value isn't {@code null}, or the value produced by
   *          the supplier otherwise.
   *  @throws X If an error occurs inside either the mapping function or the supplier.
   */
  public static <T, U, X extends Throwable> U ifEither(T value, 
                                                       CheckedFunction<? super T, ? extends U, X> mapperIfPresent, 
                                                       CheckedSupplier<? extends U, X> supplierIfAbsent) throws X {
    return value != null ? mapperIfPresent.apply(value) : supplierIfAbsent.get();
  }

  /**
   *  Attempts to return the value produced by the given {@code preferredSupplier} if this can be done
   *  successfully without throwing an exception, otherwise the return value is sourced from the
   *  {@code fallbackSupplier}. <p>
   *  
   *  The preferred supplier may throw a checked exception (which will be silently discarded). The
   *  fallback supplier, on the other hand, is not permitted to throw a checked exception.
   *  
   *  @param <T> Value type.
   *  @param preferredSupplier The supplier to attempt first.
   *  @param fallbackSupplier The fallback supplier, if the preferred one fails.
   *  @return The resulting value, sourced from either the preferred or the fallback supplier.
   */
  public static <T> T ifThrew(CheckedSupplier<? extends T, ? extends Throwable> preferredSupplier, 
                              Supplier<? extends T> fallbackSupplier) {
    try {
      return preferredSupplier.get();
    } catch (Throwable e) {
      return fallbackSupplier.get();
    }
  }

  /**
   *  Obtains a checked supplier of {@code null}. <p>
   *  
   *  The supplier's type parameter indicates that it may throw a {@link RuntimeException},
   *  making it compatible with statements that don't throw checked exceptions. In actual fact
   *  no exception will ever be thrown from this supplier.
   *  
   *  @param <T> Value type.
   *  @return A {@link CheckedSupplier} of {@code null}.
   */
  public static <T> CheckedSupplier<T, RuntimeException> giveNull() {
    return () -> null;
  }

  /**
   *  Obtains a conventional supplier of {@code null}.
   *  
   *  @param <T> Value type.
   *  @return A {@link Supplier} of {@code null}.
   */
  public static <T> Supplier<T> givePlainNull() {
    return () -> null;
  }
  
  /**
   *  Obtains a checked supplier of the given value. <p>
   *  
   *  The supplier's type parameter indicates that it may throw a {@link RuntimeException},
   *  making it compatible with statements that don't throw checked exceptions. In actual fact
   *  no exception will ever be thrown from this supplier.
   *  
   *  @param <T> Value type.
   *  @param value The value to supply.
   *  @return A {@link CheckedSupplier} of the given {@code value}.
   */
  public static <T> CheckedSupplier<T, RuntimeException> give(T value) {
    return () -> value;
  }

  /**
   *  Obtains a conventional supplier of the given value.
   *  
   *  @param <T> Value type.
   *  @param value The value to supply.
   *  @return A {@link Supplier} of the given {@code value}.
   */
  public static <T> Supplier<T> givePlain(T value) {
    return () -> value;
  }

  /**
   *  Curries a {@link ExceptionHandler} with a specific error {@code summary}, so that it can be used
   *  as a simpler {@link Throwable} {@link Consumer}.
   *  
   *  @param summary The error summary to feed to the {@code exceptionHandler}.
   *  @param exceptionHandler Handles errors.
   *  @return A curried {@link Throwable} {@link Consumer}.
   */
  public static Consumer<Throwable> withSummary(String summary, ExceptionHandler exceptionHandler) {
    return cause -> exceptionHandler.onException(summary, cause);
  }
  
  /**
   *  Functional variant of the try-catch statement.
   *  
   *  @param errorProneRunnable A {@link CheckedRunnable} to try.
   *  @param onError The {@link Throwable} {@link Consumer} that will handle any exceptions.
   */
  public static void tryCatch(CheckedRunnable<?> errorProneRunnable, Consumer<Throwable> onError) {
    try {
      errorProneRunnable.run();
    } catch (Throwable e) {
      onError.accept(e);
    }
  }
  
  /**
   *  Functional variant of the try-catch statement that can return a value.
   *  
   *  @param <T> Return type.
   *  @param errorProneSupplier A {@link CheckedSupplier} to try.
   *  @param defaultValueSupplier If the supplier fails, the default supplier is used to return a value.
   *  @param onError The {@link Throwable} {@link Consumer} that will handle any exceptions.
   *  @return The resulting value.
   */
  public static <T> T tryCatch(CheckedSupplier<? extends T, ?> errorProneSupplier, 
                               Supplier<? extends T> defaultValueSupplier, 
                               Consumer<Throwable> onError) {
    try {
      return errorProneSupplier.get();
    } catch (Throwable e) {
      onError.accept(e);
      return defaultValueSupplier.get();
    }
  }
  
  /**
   *  Ignores exceptions. For use with {@link #tryCatch(CheckedRunnable, Consumer)} and
   *  {@link #tryCatch(CheckedSupplier, Supplier, Consumer)}.
   *  
   *  @return A no-op {@link Consumer} of {@link Throwable}.
   */
  public static Consumer<Throwable> ignoreException() {
    return __ -> {};
  }
  
  /**
   *  Creates a {@link Comparator} that works by extracting an field from the encompassing class using the
   *  given {@code fieldExtractor} and then comparing the extracted fields using the given 
   *  {@code fieldComparator}. <p>
   *  
   *  Some examples:<p>
   *  
   *  Sort strings by their length:<br>
   *  <pre>
   *  final var strings = asList("dddd", "a", "ccc", "bb");
   *  Collections.sort(strings, byField(String::length, Comparator.naturalOrder()));
   *  </pre>
   *  
   *  Sort points using a {@link ChainedComparator} with priority given to the X-coordinate, followed by
   *  the Y-coordinate: <br>
   *  <pre>
   *  {@code
   *  final var points = asList(new Point(2, 2), new Point(0, 0), new Point(1, 2), new Point(1, 0));
   *  Collections.sort(points, 
   *                   new ChainedComparator<Point>()
   *                   .chain(byField(Point::getX, Comparator.naturalOrder()))
   *                   .chain(byField(Point::getY, Comparator.naturalOrder())));
   *  }
   *  </pre>
   *  
   *  @param <T> Encompassing type.
   *  @param <U> Field type.
   *  @param fieldExtractor A way of extracting the field from the encompassing type (typically a method 
   *                        reference to an existing getter).
   *  @param fieldComparator A way of comparing the extracted fields.
   *  @return The resulting {@link Comparator} instance.
   */
  public static <T, U> Comparator<T> byField(Function<T, U> fieldExtractor, Comparator<? super U> fieldComparator) {
    return (t0, t1) -> {
      final U field0 = fieldExtractor.apply(t0);
      final U field1 = fieldExtractor.apply(t1);
      return fieldComparator.compare(field0, field1);
    };
  }
  
  /**
   *  Coerces a given {@link CheckedConsumer} into an equivalent {@link CheckedFunction} returning {@link Void}, 
   *  allowing the consumer to be used where a function is expected, and where the return value is irrelevant.
   *  
   *  @param <U> The input type.
   *  @param <X> The exception type.
   *  @param consumer The consumer.
   *  @return The {@link Void}-returning {@link Function}.
   */
  public static <U, X extends Throwable> CheckedFunction<U, Void, X> voidFunction(CheckedConsumer<? super U, ? extends X> consumer) {
    return u -> {
      consumer.accept(u);
      return null;
    };
  }
  
  /**
   *  Produces a refined predicate that is applied to a transformed value of a tested element. Typically, the
   *  transformation is to extract a field from the object under test.
   *  
   *  @param <T> Encompassing type.
   *  @param <U> Field type.
   *  @param fieldExtractor A way of extracting the field from the encompassing type (typically a method 
   *                        reference to an existing getter).
   *  @param fieldPredicate The predicate applied to the extracted field.
   *  @return The resulting {@link Predicate} instance.
   */
  public static <T, U> Predicate<T> fieldPredicate(Function<? super T, ? extends U> fieldExtractor, Predicate<? super U> fieldPredicate) {
    return element -> fieldPredicate.test(fieldExtractor.apply(element));
  }
}
