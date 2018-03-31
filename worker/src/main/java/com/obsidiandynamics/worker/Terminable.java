package com.obsidiandynamics.worker;

@FunctionalInterface
public interface Terminable {
  Joinable terminate();
}
