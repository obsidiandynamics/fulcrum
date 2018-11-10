package com.obsidiandynamics.json.fieldpatch;

import static com.obsidiandynamics.func.Functions.*;

import java.util.*;
import java.util.function.*;

import com.obsidiandynamics.func.*;

/**
 *  A field patch is analogous to a {@link java.util.Optional} for document attributes, used in
 *  operations where an existing object requires one or more of its attributes patched
 *  (typically using a {@code PATCH} HTTP verb).
 *
 *  @param <T> Value type.
 */
public interface FieldPatch<T> {
  static final FieldPatch<?> NULL_INSTANCE = new StandardFieldPatch<>(null);

  T get();
  
  default String baseToString() {
    return getClass().getSimpleName() + " [" + get() + "]";
  }
  
  default int baseHashCode() {
    return Objects.hashCode(get());
  }
  
  default boolean baseEquals(FieldPatch<T> that) {
    return Objects.equals(get(), that.get());
  }
  
  static <T> FieldPatch<T> of(T value) {
    return new StandardFieldPatch<>(mustExist(value, "Value cannot be null"));
  }
  
  static <T> FieldPatch<T> nullableOf(T value) {
    return value != null ? new StandardFieldPatch<>(value) : ofNull();
  }

  static <T> FieldPatch<T> ofNull() {
    return Classes.cast(NULL_INSTANCE);
  }
  
  static <T, U> void ifExists(FieldPatch<T> patch, Function<T, U> valueTransform, Consumer<? super U> valueConsumer) {
    ifExists(patch, value -> valueConsumer.accept(valueTransform.apply(value)));
  }
  
  static <T> void ifExists(FieldPatch<T> patch, Consumer<? super T> valueConsumer) {
    if (patch != null) valueConsumer.accept(patch.get());
  }
  
  static final class StandardFieldPatch<T> implements FieldPatch<T> {
    private final T value;
    
    StandardFieldPatch(T value) {
      this.value = value;
    }

    @Override
    public T get() {
      return value;
    }
    
    @Override
    public int hashCode() {
      return baseHashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if (obj instanceof StandardFieldPatch) {
        return baseEquals(Classes.cast(obj));
      } else {
        return false;
      }
    }

    @Override
    public String toString() {
      return baseToString();
    }
  }
}
