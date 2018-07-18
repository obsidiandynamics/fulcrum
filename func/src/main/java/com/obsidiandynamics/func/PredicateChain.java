package com.obsidiandynamics.func;

import java.util.*;
import java.util.function.*;

public final class PredicateChain<T> {
  private final List<Predicate<? super T>> predicates = new ArrayList<>();
  
  public PredicateChain<T> chain(Predicate<? super T> predicate) {
    predicates.add(predicate);
    return this;
  }

  public Predicate<T> allMatch() {
    return t -> {
      for (Predicate<? super T> predicate : predicates) {
        if (! predicate.test(t)) return false;
      }
      return true;
    };
  }

  public Predicate<T> anyMatch() {
    return t -> {
      for (Predicate<? super T> predicate : predicates) {
        if (predicate.test(t)) return true;
      }
      return false;
    };
  }
  
  @SafeVarargs
  public static <T> Predicate<T> allOf(Predicate<? super T>... predicates) {
    final PredicateChain<T> predicateChain = new PredicateChain<>();
    for (Predicate<? super T> predicate : predicates) {
      predicateChain.chain(predicate);
    }
    return predicateChain.allMatch();
  }
  
  @SafeVarargs
  public static <T> Predicate<T> anyOf(Predicate<? super T>... predicates) {
    final PredicateChain<T> predicateChain = new PredicateChain<>();
    for (Predicate<? super T> predicate : predicates) {
      predicateChain.chain(predicate);
    }
    return predicateChain.anyMatch();
  }
}
