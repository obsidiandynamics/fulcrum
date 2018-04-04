package com.obsidiandynamics.dyno;

import java.util.concurrent.*;

public interface BenchmarkTarget extends Callable<Object> {
  default void setup() throws Exception {}
  
  default void tearDown() throws Exception {}
}
