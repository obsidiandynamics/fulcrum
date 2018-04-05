package com.obsidiandynamics.dyno;

@FunctionalInterface
public interface BenchmarkTarget {
  default void setup() throws Exception {}
  
  void cycle(Abyss abyss) throws Exception;
  
  default void tearDown() throws Exception {}
}
