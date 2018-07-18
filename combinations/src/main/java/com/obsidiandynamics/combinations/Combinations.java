package com.obsidiandynamics.combinations;

import java.util.*;

import com.obsidiandynamics.func.*;

/**
 *  Generates all column combinations of a given jagged 2D array; that is, the 
 *  complete set of vectors resulting when each element in every vector is taken 
 *  in combination with elements of all other vectors.<p>
 *  
 *  Example: the 2D array <br>
 *  [<br>
 *  &nbsp;&nbsp;[a,b,c]<br>
 *  &nbsp;&nbsp;[d,e]<br>
 *  ]<br>
 *  will yield combination vectors<br><br>
 *  [a,d],<br>
 *  [a,e],<br>
 *  [b,d],<br>
 *  [b,e],<br>
 *  [c,d],<br>
 *  [c,e]<br>
 *  <p>
 *  
 *  This class can be used in a stateful manner, by instantiating it with the
 *  vectors to combine, then calling {@link #enumerate()} or using the iterator to
 *  obtain all discrete combinations.<p>
 *  Alternatively, the static {@link #enumerate(Object[][])} method may be used on 
 *  its own, without the need for instantiating {@link Combinations}.
 *  
 *  @param <E> Element type.
 */
public final class Combinations<E> implements Iterable<List<E>> {
  private final E[][] matrix;
  
  public Combinations(List<List<E>> matrix) {
    this(Classes.<E[][]>cast(toMatrix(matrix)));
  }
  
  public Combinations(E[][] matrix) {
    Indexer.validateDimensions(getDimensions(matrix));
    this.matrix = matrix;
  }
  
  /**
   *  Obtains the number of combination vectors that can be produced from
   *  the given matrix.
   *  
   *  @param matrix The element matrix.
   *  @return The number of combinations.
   */
  public static int size(Object[][] matrix) {
    return Indexer.size(getDimensions(matrix));
  }

  /**
   *  Obtains the dimensions for the hypercube that corresponds to the given
   *  matrix.
   *  
   *  @param matrix The element matrix.
   *  @return The hypercube dimensions.
   */
  public static int[] getDimensions(Object[][] matrix) {
    final int length = matrix.length;
    final int[] dimensions = new int[length];
    for (int i = 0; i < length; i++) {
      dimensions[i] = matrix[i].length;
    }
    return dimensions;
  }
  
  /**
   *  Obtains the combination vector at the given hypercube location.<p>
   *  
   *  This variant will instantiate a combination array internally and will
   *  wrap it in a typed {@link List} prior to returning.
   *  
   *  @param <E> Element type.
   *  @param indices The hypercube location.
   *  @param matrix The element matrix.
   *  @return The combination vector at the given location.
   */
  public static <E> List<E> get(int[] indices, E[][] matrix) {
    final E[] combination = Classes.<E[]>cast(new Object[matrix.length]);
    return Arrays.asList(get(indices, matrix, combination));
  }

  /**
   *  Obtains the combination vector at the given hypercube location, populating
   *  then given {@code combination} array.<p>
   *  
   *  This variant expects the caller to pass in the {@code combination} array argument,
   *  thereby avoiding array instantiation when this method is called repeatedly.
   *  
   *  @param <E> Element type.
   *  @param indices The hypercube location.
   *  @param matrix The element matrix.
   *  @param combination The combination vector to populate.
   *  @return The combination vector at the given location; the populated 
   *          {@code combination} array.
   */
  public static <E> E[] get(int[] indices, E[][] matrix, E[] combination) {
    for (int i = 0; i < indices.length; i++) {
      combination[i] = matrix[i][indices[i]];
    }
    return combination;
  }

  /**
   *  Obtains an enumeration of all combination vectors.
   *  
   *  @param <E> Element type.
   *  @param matrix The element matrix.
   *  @return The list of combination vectors.
   */
  public static <E> List<List<E>> enumerate(E[][] matrix) {
    final int[] dimensions = getDimensions(matrix);
    final int size = Indexer.size(dimensions);
    final List<List<E>> list = new ArrayList<>(size);
    final int[] indices = new int[dimensions.length];
    for (int i = 0; i < size; i++) {
      list.add(get(Indexer.getIndices(i, dimensions, indices), matrix));
    }
    return list;
  }

  /**
   *  A utility for converting a list of lists to an equivalent 2D rectangular
   *  array matrix.
   *  
   *  @param <E> Element type.
   *  @param matrix The list of lists.
   *  @return The corresponding matrix.
   */
  public static <E> Object[][] toMatrix(List<List<E>> matrix) {
    final int outerSize = matrix.size();
    final Object[][] vectors = new Object[outerSize][];
    for (int i = 0; i < outerSize; i++) {
      vectors[i] = matrix.get(i).toArray();
    }
    return vectors;
  }
  
  /**
   *  Obtains the number of combination vectors that can be produced from
   *  the underlying matrix.
   *  
   *  @return The number of combinations.
   */
  public int size() {
    return size(matrix);
  }
  
  /**
   *  Obtains the dimensions for the hypercube that corresponds to the underlying
   *  matrix.
   *  
   *  @return The hypercube dimensions.
   */
  public int[] getDimensions() {
    return getDimensions(matrix);
  }

  /**
   *  Obtains the combination vector at the given hypercube location.
   *  
   *  @param indices The hypercube location.
   *  @return The combination vector at the given location.
   */
  public List<E> get(int... indices) {
    return get(indices, matrix);
  }
  
  /**
   *  Obtains an enumeration of all combination vectors.
   *  
   *  @return The list of combination vectors.
   */
  public List<List<E>> enumerate() {
    return enumerate(matrix);
  }

  @Override
  public Iterator<List<E>> iterator() {
    return new Iterator<List<E>>() {
      private final Indexer indexer = new Indexer(getDimensions(matrix));
      private final Iterator<int[]> indexIterator = indexer.iterator();
      
      @Override 
      public boolean hasNext() {
        return indexIterator.hasNext();
      }

      @Override 
      public List<E> next() {
        return get(indexIterator.next(), matrix);
      }
    };
  }
}
