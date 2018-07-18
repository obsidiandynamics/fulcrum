package com.obsidiandynamics.combinations;

import java.util.*;

/**
 *  Translates the offset in a given hypervector into a set of indices in a
 *  corresponding hypercube.<p>
 *  
 *  This class can be used in a stateful manner, by instantiating it with the
 *  dimensions of the hypercube, then iterating over the indices.<p>
 *  
 *  Alternatively, the static {@link #getIndices(int, int[], int[])} method may be used
 *  on its own, without the need for instantiating the {@link Indexer}, assuming
 *  the caller maintains state.
 */
public final class Indexer implements Iterable<int[]> {
  /** The dimensions of the hypercube. */
  private final int[] dimensions;
  
  public Indexer(int... dimensions) {
    validateDimensions(dimensions);
    this.dimensions = dimensions;
  }
  
  public int[] getDimensions() {
    return dimensions;
  }
  
  /**
   *  Validates the hypercube dimensions.
   *  
   *  @param dimensions The dimensions.
   *  @throws IllegalArgumentException If validation fails.
   */
  public static void validateDimensions(int... dimensions) {
    if (dimensions.length == 0) throw new IllegalArgumentException("Cannot create with empty dimensions");
  }

  /**
   *  Computes the hypercube indices for the given hypervector offset, based
   *  on the dimensions of this hypervector.
   *  
   *  @param offset The hypervector offset.
   *  @param dimensions The hypercube dimensions.
   *  @param indices The indices array to populate.
   *  @return The corresponding hypercube indices; the populated {@code indices}.
   */
  public static int[] getIndices(int offset, int[] dimensions, int[] indices) {
    validateDimensions(dimensions);
    int left = offset;
    for (int caret = indices.length; --caret >= 0; ) {
      final int sizeOfDimension = dimensions[caret];
      if (sizeOfDimension == 0) throw new IndexOutOfBoundsException("Index out of range: " + offset);
      final int quotient = left / sizeOfDimension;
      final int remainder = left % sizeOfDimension;
      indices[caret] = remainder;
      left = quotient;
    }
    if (left != 0) throw new IndexOutOfBoundsException("Index out of range: " + offset);
    return indices;
  }
  
  /**
   *  Obtains the volume of the given hypercube or, equivalently, the length of the
   *  corresponding hypervector.
   *  
   *  @param dimensions The hypercube dimensions.
   *  @return The hypercube volume.
   */
  public static int size(int... dimensions) {
    validateDimensions(dimensions);
    int product = 1;
    for (int d : dimensions) {
      product *= d;
    }
    return product;
  }
  
  /**
   *  Computes the hypercube indices for the underlying hypervector offset, based
   *  on the dimensions of this hypervector.<p>
   *  
   *  This variant creates the indices array based on the number of dimensions in the
   *  hypercube.
   *  
   *  @param offset The hypervector offset.
   *  @return The corresponding hypercube indices.
   */
  public int[] getIndices(int offset) {
    return getIndices(offset, new int[dimensions.length]);
  }
  
  /**
   *  Computes the hypercube indices for the underlying hypervector offset, based
   *  on the dimensions of this hypervector.<p>
   *  
   *  This variant expects the caller to pass in the {@code indices} array argument,
   *  thereby avoiding array instantiation when this method is called repeatedly.
   *  
   *  @param offset The hypervector offset.
   *  @param indices The indices array to mutate.
   *  @return The corresponding hypercube indices; the populated {@code indices}.
   */
  public int[] getIndices(int offset, int[] indices) {
    return getIndices(offset, dimensions, indices);
  }
  
  /**
   *  Obtains the volume of the underlying hypercube or, equivalently, the length of the
   *  underlying hypervector.
   *  
   *  @return The hypercube volume.
   */
  public int size() {
    return size(dimensions);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(dimensions);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof Indexer) {
      final Indexer that = (Indexer) obj;
      return Arrays.equals(dimensions, that.dimensions);
    } else {
      return false;
    }
  }
  
  @Override
  public String toString() {
    return Indexer.class.getSimpleName() + " [dimensions=" + Arrays.toString(dimensions) + "]";
  }

  /**
   *  Iterates over all indices in the hypercube, by iteratively stepping through the offsets
   *  in a hypervector and mapping each offset to a location in the hypercube.
   */
  @Override
  public Iterator<int[]> iterator() {
    return new Iterator<int[]>() {
      private final int length = size();
      private final int[] indices = new int[dimensions.length];
      private int offset = 0;
      
      @Override 
      public boolean hasNext() {
        return offset < length;
      }

      @Override 
      public int[] next() {
        return getIndices(offset++, dimensions, indices);
      }
    };
  }
}
