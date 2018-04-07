package com.obsidiandynamics.dyno;

import com.obsidiandynamics.func.*;

public final class BenchmarkResult {
  private final long durationMillis;
  private final double score;
  private final Object result;

  BenchmarkResult(long durationMillis, double score, Object result) {
    this.durationMillis = durationMillis;
    this.score = score;
    this.result = result;
  }
  
  public long getDuration() {
    return durationMillis;
  }

  public double getScore() {
    return score;
  }

  public <T> T getResult() {
    return Classes.cast(result);
  }
  
  @Override
  public String toString() {
    return String.format("took %,d ms, score: %6.3e, result: %s", durationMillis, score, result);
  }
}
