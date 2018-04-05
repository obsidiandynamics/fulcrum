package com.obsidiandynamics.dyno;

import com.obsidiandynamics.func.*;

@FunctionalInterface
public interface BenchmarkDriver {
  BenchmarkResult run(int threads, 
                      int warmupTimeMillis, 
                      int benchTimeMillis, 
                      ExceptionHandler exceptionHandler, 
                      Class<? extends BenchmarkTarget> targetClass);
}
