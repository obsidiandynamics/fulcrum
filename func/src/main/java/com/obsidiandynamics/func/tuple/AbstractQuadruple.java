package com.obsidiandynamics.func.tuple;

/**
 *  The base class for implementing ordered quadruples (4-tuples), notably the default {@link Quadruple}
 *  implementation. <p>
 *  
 *  New ordered quadruple implementations can be defined by directly subclassing {@link AbstractQuadruple},
 *  as in the following example. <br>
 *  <pre>
 *  class Colour extends AbstractQuadruple&lt;Integer, Integer, Integer, Integer&gt; {
 *    public Colour(int red, int green, int blue, int alpha) {
 *      super(red, green, blue, alpha);
 *    }
 *    
 *    public int getRed() {
 *      return getFirstElement();
 *    }
 *    
 *    public int getGreen() {
 *      return getSecondElement();
 *    }
 *    
 *    public int getBlue() {
 *      return getThirdElement();
 *    }
 *    
 *    public int getAlpha() {
 *      return getFourthElement();
 *    }
 *  }
 *  </pre>
 *
 *  @param <A> First element type.
 *  @param <B> Second element type.
 *  @param <C> Third element type.
 *  @param <D> Fourth element type.
 */
public abstract class AbstractQuadruple<A, B, C, D> extends AbstractTuple {
  protected AbstractQuadruple(A first, B second, C third, D fourth) {
    super(first, second, third, fourth);
  }
  
  protected final A getFirstElement() { return get(0); }
  
  protected final B getSecondElement() { return get(1); }
  
  protected final C getThirdElement() { return get(2); }
  
  protected final D getFourthElement() { return get(3); }
}
