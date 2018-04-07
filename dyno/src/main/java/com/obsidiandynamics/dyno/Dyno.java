package com.obsidiandynamics.dyno;

import java.util.function.*;

/**
 *  Bootstraps the benchmark.
 */
public final class Dyno {
  static Consumer<BenchmarkResult> defaultResultConsumer = __result -> {};
  
  /** The driver that will execute the actual benchmark. */
  private BenchmarkDriver driver = new JmhDriver();
  
  /** Number of threads to use. */
  private int threads = 1;
  
  /** Fraction of time dedicated to warmup. (On top of benchmark time). */
  private double warmupFrac = .01f;
  
  /** Duration of a timed run. */
  private int benchmarkTimeMillis = 5;
  
  /** Class containing the benchmark body and setup/tear-down routines. */
  private Class<? extends BenchmarkTarget> targetClass;
  
  /** Optional: where to send the result to. */
  private Consumer<BenchmarkResult> resultConsumer = defaultResultConsumer;
  
  public Dyno withDriver(BenchmarkDriver driver) {
    this.driver = driver;
    return this;
  }
  
  public Dyno withThreads(int threads) {
    this.threads = threads;
    return this;
  }

  public Dyno withBenchmarkTime(int benchmarkTimeMillis) {
    this.benchmarkTimeMillis = benchmarkTimeMillis;
    return this;
  }
  
  public Dyno withWarmupFraction(double warmupFrac) {
    this.warmupFrac = warmupFrac;
    return this;
  }

  public Dyno withTarget(Class<? extends BenchmarkTarget> targetClass) {
    this.targetClass = targetClass;
    return this;
  }
  
  public Dyno withOutput(Consumer<BenchmarkResult> resultConsumer) {
    this.resultConsumer = resultConsumer;
    return this;
  }
  
  public BenchmarkResult run() {
    final int warmupTimeMillis = (int) (benchmarkTimeMillis * warmupFrac);
    final BenchmarkResult result = driver.run(threads, warmupTimeMillis, benchmarkTimeMillis, targetClass);
    resultConsumer.accept(result);
    return result;
  }
}
