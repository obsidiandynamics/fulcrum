package com.obsidiandynamics.func.tuple;

/**
 *  The base class for implementing ordered triples (3-tuples), notably the default {@link Triple}
 *  implementation. <p>
 *  
 *  New ordered triple implementations can be defined by directly subclassing {@link AbstractTriple},
 *  as in the following example. <br>
 *  <pre>
 *  class Colour extends AbstractTriple&lt;Integer, Integer, Integer&gt; {
 *    public Colour(int red, int green, int blue) {
 *      super(red, green, blue);
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
 *  }
 *  </pre>
 *
 *  @param <A> First element type.
 *  @param <B> Second element type.
 *  @param <C> Third element type.
 */
public abstract class AbstractTriple<A, B, C> extends AbstractTuple {
  protected AbstractTriple(A first, B second, C third) {
    super(first, second, third);
  }
  
  protected final A getFirstElement() { return get(0); }
  
  protected final B getSecondElement() { return get(1); }
  
  protected final C getThirdElement() { return get(2); }
}
