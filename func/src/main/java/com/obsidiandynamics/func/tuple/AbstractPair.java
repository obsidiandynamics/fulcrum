package com.obsidiandynamics.func.tuple;

/**
 *  The base class for implementing ordered pairs (2-tuples), notably the default {@link Pair}
 *  implementation. <p>
 *  
 *  New ordered pair implementations can be defined by directly subclassing {@link AbstractPair},
 *  as in the following example. <br>
 *  <pre>
 *  class Point extends AbstractPair&lt;Integer, Integer&gt; {
 *    public Point(int x, int y) {
 *      super(x, y);
 *    }
 *    
 *    public int getX() {
 *      return getFirstElement();
 *    }
 *    
 *    public int getY() {
 *      return getSecondElement();
 *    }
 *  }
 *  </pre>
 *
 *  @param <A> First element type.
 *  @param <B> Second element type.
 */
public abstract class AbstractPair<A, B> extends AbstractTuple {
  protected AbstractPair(A first, B second) {
    super(first, second);
  }
  
  protected final A getFirstElement() { return get(0); }
  
  protected final B getSecondElement() { return get(1); }
}
