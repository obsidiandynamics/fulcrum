package com.obsidiandynamics.func.tuple;

import com.obsidiandynamics.func.*;

/**
 *  An immutable ordered pair (2-tuple). <p>
 *  
 *  Specific ordered pair implementations can be constructed by subclassing {@link AbstractPair}
 *  directly.
 *  
 *  @param <A> First element type.
 *  @param <B> Second element type.
 */
public final class Pair<A, B> extends AbstractPair<A, B> {
  private static final Pair<?, ?> EMPTY = new Pair<>(null, null);
  
  private Pair(A first, B second) {
    super(first, second);
  }
  
  public A getFirst() {
    return getFirstElement();
  }
  
  public B getSecond() {
    return getSecondElement();
  }
  
  @Override
  public Pair<A, B> clone() {
    return of(getFirst(), getSecond());
  }
  
  public static <A, B> Pair<A, B> empty() {
    return Classes.cast(EMPTY);
  }
  
  public static <A, B> Pair<A, B> of(A first, B second) {
    if (first == null && second == null) {
      return empty();
    } else {
      return new Pair<>(first, second);
    }
  }
}
