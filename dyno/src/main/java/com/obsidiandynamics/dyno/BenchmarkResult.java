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
    final String mainFormat = "took %,d ms, score: %6.3e";
    if (result != null) {
      return String.format(mainFormat + ", result: %s", durationMillis, score, result);
    } else {
      return String.format(mainFormat, durationMillis, score);
    }
  }
}
