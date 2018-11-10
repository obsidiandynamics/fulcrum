package com.obsidiandynamics.func.fsm;

import static com.obsidiandynamics.func.Functions.*;

import java.util.*;

/**
 *  A tuple representing a transition from one state to another.
 *  
 *  @param <S> Elemental state type.
 */
public final class Transition<S> {
  private final S from;
  
  private final S to;

  public Transition(S from, S to) {
    this.from = mustExist(from, "Missing 'from' state");
    this.to = mustExist(to, "Missing 'to' state");
  }

  public S getFrom() {
    return from;
  }

  public S getTo() {
    return to;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + Objects.hashCode(from);
    result = 31 * result + Objects.hashCode(to);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof Transition) {
      final Transition<?> that = (Transition<?>) obj;
      return Objects.equals(from, that.from) && Objects.equals(to, that.to);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return from + " -> " + to;
  }
}
