package com.obsidiandynamics.dyno;

import java.util.function.*;

public final class Microbench {
  /** The driver that will execute the actual benchmark. */
  private BenchmarkDriver driver = new JmhDriver();
  
  /** Number of threads to use. */
  private int threads = 1;
  
  /** Fraction of time dedicated to warmup. (On top of benchmark time). */
  private float warmupFrac = .01f;
  
  /** Duration of a timed run, in seconds. */
  private int benchTime = 5;
  
  /** Handles any errors occurred during the execution of the benchmark. */
  private Consumer<Exception> exceptionHandler = e -> e.printStackTrace();
  
  /** Class containing the benchmark body and setup/tear-down routines. */
  private Class<? extends BenchmarkTarget> targetClass;
  
  /** Optional: where to send the result to. */
  private Consumer<BenchmarkResult> consumer;
  
  public Microbench driver(BenchmarkDriver driver) {
    this.driver = driver;
    return this;
  }
  
  public Microbench multiThreaded() {
    return threads(Runtime.getRuntime().availableProcessors());
  }

  public Microbench threads(int threads) {
    this.threads = threads;
    return this;
  }

  public Microbench time(int benchTime) {
    this.benchTime = benchTime;
    return this;
  }
  
  public Microbench warmup(float warmupFrac) {
    this.warmupFrac = warmupFrac;
    return this;
  }

  public Microbench exceptionHandler(Consumer<Exception> exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
    return this;
  }

  public Microbench target(Class<? extends BenchmarkTarget> targetClass) {
    this.targetClass = targetClass;
    return this;
  }
  
  public Microbench output(Consumer<BenchmarkResult> consumer) {
    this.consumer = consumer;
    return this;
  }
  
  public BenchmarkResult run() {
    final int warmupTime = warmupFrac == 0 ? 0 : Math.max(1, (int) (benchTime * warmupFrac));
    final BenchmarkResult result = driver.run(threads, warmupTime, benchTime, exceptionHandler, targetClass);
    if (consumer != null) {
      consumer.accept(result);
    }
    return result;
  }
}
