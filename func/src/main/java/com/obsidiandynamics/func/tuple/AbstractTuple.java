package com.obsidiandynamics.func.tuple;

import static com.obsidiandynamics.func.Functions.*;

import java.util.*;
import java.util.function.*;

import com.obsidiandynamics.func.*;

/**
 *  Supports the construction of immutable tuples of arbitrary arity (N-tuples), providing
 *  baseline {@link #equals(Object)}, {@link #hashCode()} and {@link #toString()} implementations, 
 *  as well as a helper for implementing a {@link Comparator} and {@link Comparable}. <p>
 *
 *  The {@link #toString()} and {@link Comparator} implementations of any given element
 *  can be customised by subclassing {@link #getFormatter(int)} and {@link #getComparator(int)}
 *  methods.
 */
public abstract class AbstractTuple {
  private final Object[] elements;
  
  protected AbstractTuple(Object... elements) {
    this.elements = elements;
  }
  
  /**
   *  Obtains the element at the given index.
   *  
   *  @param <T> Element type.
   *  @param elementIndex The element index.
   *  @return The element, cast to {@code T}.
   */
  protected final <T> T get(int elementIndex) {
    return Classes.cast(elements[elementIndex]);
  }
  
  @Override
  public final int hashCode() {
    return Arrays.deepHashCode(elements);
  }
  
  @Override
  public final boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (getClass().isInstance(obj)) {
      final AbstractTuple that = (AbstractTuple) obj;
      return Arrays.deepEquals(elements, that.elements);
    } else {
      return false;
    }
  }
  
  /**
   *  Overridable {@link #toString()} behaviour for an individual element.
   *  
   *  @param elementIndex The index of the element to format.
   *  @return The {@link Function} for converting an object at the given index to a {@link String}.
   */
  protected Function<Object, String> getFormatter(int elementIndex) {
    return Object::toString;
  }
  
  /**
   *  Overridable {@link Comparator} behaviour for an individual element.
   *  
   *  @param elementIndex The index of the element to compare.
   *  @return The {@link Comparator} applicable for the given element index.
   */
  protected Comparator<?> getComparator(int elementIndex) {
    return Comparator.nullsFirst(Comparator.naturalOrder());
  }
  
  protected final int compare(AbstractTuple other) {
    return compareElements(this, other);
  }
  
  @Override
  public final String toString() {
    final StringBuilder buffer = new StringBuilder(getClass().getSimpleName());
    buffer.append(" [");
    for (int elementIndex = 0; elementIndex < elements.length; elementIndex++) {
      final String str = getFormatter(elementIndex).apply(elements[elementIndex]);
      buffer.append(str);
      if (elementIndex < elements.length - 1) {
        buffer.append(", ");
      }
    }
    buffer.append("]");
    return buffer.toString();
  }
  
  /**
   *  Obtains the standard comparator behaviour.
   *  
   *  @return A {@link Comparator} of {@link AbstractTuple} types.
   */
  public static final Comparator<AbstractTuple> defaultComparator() {
    return AbstractTuple::compareElements;
  }
  
  /**
   *  Provides the standard behaviour for comparing instances of {@link AbstractTuple}. <p>
   *  
   *  Tuples are compared element by element; for each element a {@link Comparator} is 
   *  provisioned by invoking {@link #getComparator(int)}. By default this is a
   *  null-first comparator wrapping a natural order comparator, and can be customised
   *  by overriding {@link #getComparator(int)}. <p>
   *  
   *  Note: tuple implementations must be of identical types to be compared (irrespective
   *  of their arity and element types). Attempting to compare heterogenous tuples will 
   *  result in an {@link IllegalArgumentException}.
   *  
   *  @param x The first tuple to compare.
   *  @param y The second tuple to compare.
   *  @return The result of the comparison, as per {@link Comparator} conventions.
   */
  public static final int compareElements(AbstractTuple x, AbstractTuple y) {
    mustBeEqual(x.getClass(), y.getClass(), 
                withMessage(() -> "Classes are not mutually comparable: " + x.getClass().getName() + " and " + y.getClass().getName(), 
                            IllegalArgumentException::new));
    
    for (int elementIndex = 0; elementIndex < x.elements.length; elementIndex++) {
      final Object xElement = x.elements[elementIndex];
      final Object yElement = y.elements[elementIndex];
      final Comparator<Object> comparator = Classes.<Comparator<Object>>cast(x.getComparator(elementIndex));
      final int comparison = comparator.compare(xElement, yElement);
      if (comparison != 0) {
        return comparison;
      }
    }
    return 0;
  }
}
