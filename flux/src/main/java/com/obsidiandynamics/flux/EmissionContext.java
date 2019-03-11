package com.obsidiandynamics.flux;

public interface EmissionContext<E> extends StageContext {
  void emit(E event);
  
  int remainingCapacity();
  
  default boolean hasRemainingCapacity() {
    return remainingCapacity() > 0;
  }
}
