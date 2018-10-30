package com.obsidiandynamics.func;

import java.util.function.*;

/**
 *  Utilities for working with class types.
 */
public final class Classes {
  private Classes() {}
  
  /**
   *  Performs an unchecked cast on the given value.
   * 
   *  @param <T> Value type.
   *  @param value The value to cast.
   *  @return The cast type.
   */
  @SuppressWarnings("unchecked")
  public static <T> T cast(Object value) {
    return (T) value;
  }
  
  /**
   *  Compresses the fully-qualified name of the given class into an abbreviated name in the
   *  style of: {@code a.c.s.Concorde} for a class with a fully-qualified name of
   *  {@code aircraft.civilian.supersonic.Concorde}, where the simple name of the class is 
   *  always preserved. <p>
   *  
   *  The {@code depth} parameter controls the maximum number of leading package segments
   *  to compress.
   *  
   *  @param className The fully-qualified class name.
   *  @param depth The maximum number of segments to compress.
   *  @return The compressed name.
   */
  public static String compressPackage(String className, int depth) {
    final String[] frags = className.split("\\.");
    final StringBuilder formatted = new StringBuilder();
    for (int i = 0; i < frags.length; i++) {
      if (formatted.length() != 0) formatted.append('.');
      if (i < depth && i < frags.length - 1) {
        formatted.append(frags[i].charAt(0));
      } else {
        formatted.append(frags[i]);
      }
    }
    return formatted.toString();
  }
  
  /**
   *  Coerces a given value to a target type by invoking the supplied {@code mapper} if the value
   *  does not match the target type. Otherwise, if the value is already of the target type, it is 
   *  cast and returned as-is. (The declared target type must be more specific than the 
   *  declared source type.) A {@code null} value is returned as-is. <p>
   *  
   *  The following examples illustrate the efficient coercion of an {@link Object} (which might be a
   *  {@link String} or an {@link Integer}) to an {@link Integer}: <br>
   *  
   *  <pre>
   *  {@code
   *  // coercion takes place through string parsing
   *  final Object value = "42";
   *  final Integer intValue = Classes.coerce(value, Integer.class, v -> Integer.parseInt(String.valueOf(v)));
   *  }
   *  </pre>
   *  
   *  <pre>
   *  {@code
   *  // similarly, because we force to a string first, the following also works
   *  final Object value = new BigDecimal("42");
   *  final Integer intValue = Classes.coerce(value, Integer.class, v -> Integer.parseInt(String.valueOf(v)));
   *  }
   *  </pre>
   *  
   *  <pre>
   *  {@code
   *  // coercion here is merely a result of type casting
   *  final Object value = 42;
   *  final Integer intValue = Classes.coerce(value, Integer.class, v -> Integer.parseInt(String.valueOf(v)));
   *  }
   *  </pre>
   *  
   *  Coercion is also useful for conditionally unwrapping container exception types and coercing any 
   *  non-conforming exceptions to a uniform type, as shown in the following example: <br>
   *  
   *  <pre>
   *  try {
   *    return future.get();
   *  } catch (ExecutionException e) {
   *    throw Classes.coerce(e.getCause(), IOException.class, IOException::new);
   *  }
   *  </pre>
   *  
   *  @param <U> Source type.
   *  @param <V> Target type.
   *  @param value The value to coerce.
   *  @param targetType The target type.
   *  @param mapper The mapping function used for coercion.
   *  @return The coerced value.
   */
  public static <U, V extends U> V coerce(U value, Class<V> targetType, Function<? super U, ? extends V> mapper) {
    return value == null || targetType.isInstance(value) ? targetType.cast(value) : mapper.apply(value);
  }
}
