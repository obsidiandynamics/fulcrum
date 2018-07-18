package com.obsidiandynamics.func;

import java.util.*;

public final class ChainedComparator<T> implements Comparator<T> {
  private final List<Comparator<? super T>> comparators = new ArrayList<>();
  
  public ChainedComparator<T> chain(Comparator<? super T> comparator) {
    comparators.add(comparator);
    return this;
  }

  @Override
  public int compare(T obj0, T obj1) {
    for (Comparator<? super T> comparator : comparators) {
      final int comparison = comparator.compare(obj0, obj1);
      if (comparison != 0) {
        return comparison;
      }
    }
    return 0;
  }
}
