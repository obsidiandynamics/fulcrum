package com.obsidiandynamics.dyno;

import com.obsidiandynamics.func.*;

public final class BenchmarkResult {
  private final long durationMillis;
  private final double primaryScore;
  private final Object result;

  BenchmarkResult(long durationMillis, double primaryScore, Object result) {
    this.durationMillis = durationMillis;
    this.primaryScore = primaryScore;
    this.result = result;
  }
  
  public long getDuration() {
    return durationMillis;
  }

  public double getPrimaryScore() {
    return primaryScore;
  }

  public <T> T getResult() {
    return Classes.cast(result);
  }
  
  @Override
  public String toString() {
    return String.format("took %,d ms, score: %6.3e, result: %s", durationMillis, primaryScore, result);
  }
}
