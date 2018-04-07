package com.obsidiandynamics.dyno;

@FunctionalInterface
public interface BenchmarkDriver {
  BenchmarkResult run(int threads, 
                      int warmupTimeMillis, 
                      int benchmarkTimeMillis, 
                      Class<? extends BenchmarkTarget> targetClass);
}
