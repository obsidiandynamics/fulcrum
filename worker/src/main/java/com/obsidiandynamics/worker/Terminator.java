package com.obsidiandynamics.worker;

import java.util.*;
import java.util.stream.*;

/**
 *  An accumulator for composing operations over instances of type {@link Terminable}. Rather than terminating
 *  each individual instance, the latter can be added to this accumulator (itself being a {@link Terminable}) 
 *  and collectively terminated with a single method call.
 */
public final class Terminator extends FluentOperatingSet<Terminable, Terminator> implements Terminable {
  private Terminator() {}
  
  @Override
  public Joinable terminate() {
    final List<Joinable> joinables = elements.stream().map(t -> t.terminate()).collect(Collectors.toList());
    return timeoutMillis -> Joinable.joinAll(timeoutMillis, joinables);
  }
  
  public static Terminator of(Collection<? extends Terminable> terminables) {
    return new Terminator().add(terminables);
  }
  
  public static Terminator of(Terminable... terminables) {
    return new Terminator().add(terminables);
  }
  
  public static Terminator blank() {
    return new Terminator();
  }
}
