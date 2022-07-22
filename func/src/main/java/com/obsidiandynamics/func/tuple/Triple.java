package com.obsidiandynamics.func.tuple;

import com.obsidiandynamics.func.*;

/**
 *  An immutable ordered triple (3-tuple). <p>
 *  
 *  Specific ordered triple implementations can be constructed by subclassing {@link AbstractTriple}
 *  directly.
 *  
 *  @param <A> First element type.
 *  @param <B> Second element type.
 *  @param <C> Third element type.
 */
public final class Triple<A, B, C> extends AbstractTriple<A, B, C> {
  private static final Triple<?, ?, ?> EMPTY = new Triple<>(null, null, null);
  
  private Triple(A first, B second, C third) {
    super(first, second, third);
  }
  
  public A getFirst() {
    return getFirstElement();
  }
  
  public B getSecond() {
    return getSecondElement();
  }
  
  public C getThird() {
    return getThirdElement();
  }

  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  public Triple<A, B, C> clone() {
    return of(getFirst(), getSecond(), getThird());
  }
  
  public static <A, B, C> Triple<A, B, C> empty() {
    return Classes.cast(EMPTY);
  }
  
  public static <A, B, C> Triple<A, B, C> of(A first, B second, C third) {
    if (first == null && second == null && third == null) {
      return empty();
    } else {
      return new Triple<>(first, second, third);
    }
  }
}
