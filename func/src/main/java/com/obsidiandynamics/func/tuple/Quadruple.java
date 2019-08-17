package com.obsidiandynamics.func.tuple;

import com.obsidiandynamics.func.*;

/**
 *  An immutable ordered quadruple (4-tuple). <p>
 *  
 *  Specific ordered quadruple implementations can be constructed by subclassing {@link AbstractQuadruple}
 *  directly.
 *  
 *  @param <A> First element type.
 *  @param <B> Second element type.
 *  @param <C> Third element type.
 *  @param <D> Fourth element type.
 */
public final class Quadruple<A, B, C, D> extends AbstractQuadruple<A, B, C, D> {
  private static final Quadruple<?, ?, ?, ?> EMPTY = new Quadruple<>(null, null, null, null);
  
  private Quadruple(A first, B second, C third, D fourth) {
    super(first, second, third, fourth);
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
  
  public D getFourth() {
    return getFourthElement();
  }
  
  @Override
  public Quadruple<A, B, C, D> clone() {
    return of(getFirst(), getSecond(), getThird(), getFourth());
  }
  
  public static <A, B, C, D> Quadruple<A, B, C, D> empty() {
    return Classes.cast(EMPTY);
  }
  
  public static <A, B, C, D> Quadruple<A, B, C, D> of(A first, B second, C third, D fourth) {
    if (first == null && second == null && third == null && fourth == null) {
      return empty();
    } else {
      return new Quadruple<>(first, second, third, fourth);
    }
  }
}
