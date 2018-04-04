package com.obsidiandynamics.dyno;

import java.util.function.*;

public interface BenchmarkDriver {
  BenchmarkResult run(int threads, 
                      int warmupTime, 
                      int benchTime, 
                      Consumer<Exception> exceptionHandler, 
                      Class<? extends BenchmarkTarget> targetClass);
}
