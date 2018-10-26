package com.obsidiandynamics.func;

import java.lang.reflect.*;

/**
 *  Operations on immutable arrays. <p>
 *  
 *  Allows for the growth and slicing of arrays by allocating new ones, without modifying 
 *  the originals.
 */
public final class ArrayCopy {
  private ArrayCopy() {}
  
  /**
   *  Creates a new array that is a copy of the given {@code source} array, with new elements
   *  added, with contents of the source array being optionally shifted in the new array.
   *  
   *  @param <T> Array type.
   *  @param source The source array.
   *  @param addElements The number of elements to grow the new array by.
   *  @param shiftOrigin The number of elements to shift the contents in the new array.
   *  @return The new array instance.
   */
  public static <T> T grow(T source, int addElements, int shiftOrigin) {
    if (addElements < 0) throw new IllegalArgumentException("Number of elements to add cannot be negative");
    if (shiftOrigin < 0) throw new IllegalArgumentException("The shift magnitude cannot be negative");
    if (shiftOrigin > addElements) throw new IllegalArgumentException("Cannot shift by more than the number of added elements");
    
    final int length = lengthOf(source);
    final T a = clear(source, length + addElements);
    System.arraycopy(source, 0, a, shiftOrigin, length);
    return a;
  }
  
  /**
   *  Creates a new array by taking a slice of the given {@code source} array, between the bounds given
   *  by {@code fromInclusive} and {@code toExclusive}.
   *  
   *  @param <T> Array type.
   *  @param source The source array.
   *  @param fromInclusive Where to start the slice (inclusive).
   *  @param toExclusive Where to end the slice (exclusive).
   *  @return The new array instance.
   */
  public static <T> T slice(T source, int fromInclusive, int toExclusive) {
    final int length = lengthOf(source);
    if (toExclusive > length) throw new ArrayIndexOutOfBoundsException("The 'to' offset exceed the size of the array");
    if (fromInclusive < 0) throw new ArrayIndexOutOfBoundsException("The 'from' offset cannot be negative");
    if (fromInclusive > toExclusive) throw new ArrayIndexOutOfBoundsException("The 'from' and 'to' offsets may not cross");
    
    final int newLength = toExclusive - fromInclusive;
    final T a = clear(source, newLength);
    System.arraycopy(source, fromInclusive, a, 0, newLength);
    return a;
  }
  
  private static int lengthOf(Object array) {
    if (array == null) throw new IllegalArgumentException("Null array");
    if (! array.getClass().isArray()) throw new IllegalArgumentException("Not an array");
    return Array.getLength(array);
  }
  
  /**
   *  Creates a new array initialised with default values to a desired length.
   *  
   *  @param <T> Array type.
   *  @param array The original array to mimic.
   *  @param newLength The length of the new array.
   *  @return The new array instance.
   */
  public static <T> T clear(T array, int newLength) {
    return allocate(Classes.<Class<T>>cast(array.getClass()), newLength);
  }
  
  /**
   *  Allocates a new array of the give type and length.
   *  
   *  @param <T> Array type.
   *  @param arrayType The array class type.
   *  @param length The length of the new array.
   *  @return The new array instance.
   */
  public static <T> T allocate(Class<T> arrayType, int length) {
    if (arrayType == null) throw new IllegalArgumentException("Type cannot be null");
    if (! arrayType.isArray()) throw new IllegalArgumentException("Type must be an array");
    final Class<?> componentType = arrayType.getComponentType();
    return Classes.cast(Array.newInstance(componentType, length));
  }
}
