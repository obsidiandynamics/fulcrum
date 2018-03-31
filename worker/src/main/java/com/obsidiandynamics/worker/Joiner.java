package com.obsidiandynamics.worker;

import java.util.*;

/**
 *  An accumulator for composing operations over instances of type {@link Joinable}. Rather than joining on
 *  each individual instance, the latter can be added to this accumulator (itself being a {@link Joinable}) 
 *  and collectively joined on with a single method call.
 */
public final class Joiner extends FluentOperatingSet<Joinable, Joiner> implements Joinable {
  private Joiner() {}

  @Override
  public boolean join(long timeoutMillis) throws InterruptedException {
    return Joinable.joinAll(timeoutMillis, elements);
  }
  
  public static Joiner of(Collection<? extends Joinable> joinables) {
    return new Joiner().add(joinables);
  }
  
  public static Joiner of(Joinable... joinables) {
    return new Joiner().add(joinables);
  }
  
  public static Joiner blank() {
    return new Joiner();
  }
}
