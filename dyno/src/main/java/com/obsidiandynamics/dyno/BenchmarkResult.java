package com.obsidiandynamics.dyno;

public final class BenchmarkResult {
  private final double duration;
  private final double rate;

  BenchmarkResult(double duration, double rate) {
    this.duration = duration;
    this.rate = rate;
  }
  
  @Override
  public String toString() {
    return String.format("took %,.0fs, %,.2f ops/s", duration, rate);
  }
}
