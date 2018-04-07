package com.obsidiandynamics.dyno;

public final class BenchmarkError extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  BenchmarkError(Throwable cause) { super(cause); }
}