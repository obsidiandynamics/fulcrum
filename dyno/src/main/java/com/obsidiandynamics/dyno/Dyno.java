package com.obsidiandynamics.dyno;

import java.util.function.*;

import com.obsidiandynamics.func.*;

public final class Dyno {
  /** The driver that will execute the actual benchmark. */
  private BenchmarkDriver driver = new JmhDriver();
  
  /** Number of threads to use. */
  private int threads = 1;
  
  /** Fraction of time dedicated to warmup. (On top of benchmark time). */
  private double warmupFrac = .01f;
  
  /** Duration of a timed run, in seconds. */
  private int benchTimeSeconds = 5;
  
  /** Handles any errors occurred during the execution of the benchmark. */
  private ExceptionHandler exceptionHandler = ExceptionHandler.forPrintStream(System.err);
  
  /** Class containing the benchmark body and setup/tear-down routines. */
  private Class<? extends BenchmarkTarget> targetClass;
  
  /** Optional: where to send the result to. */
  private Consumer<BenchmarkResult> consumer = __result -> {};
  
  public Dyno withDriver(BenchmarkDriver driver) {
    this.driver = driver;
    return this;
  }
  
  public Dyno multiThreaded() {
    return withThreads(Runtime.getRuntime().availableProcessors());
  }

  public Dyno withThreads(int threads) {
    this.threads = threads;
    return this;
  }

  public Dyno withBenchTime(int benchTimeSeconds) {
    this.benchTimeSeconds = benchTimeSeconds;
    return this;
  }
  
  public Dyno withWarmupFraction(double warmupFrac) {
    this.warmupFrac = warmupFrac;
    return this;
  }

  public Dyno withExceptionHandler(ExceptionHandler exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
    return this;
  }

  public Dyno withTarget(Class<? extends BenchmarkTarget> targetClass) {
    this.targetClass = targetClass;
    return this;
  }
  
  public Dyno withOutput(Consumer<BenchmarkResult> consumer) {
    this.consumer = consumer;
    return this;
  }
  
  public BenchmarkResult run() {
    final int warmupTimeSeconds = (int) (benchTimeSeconds * warmupFrac);
    final BenchmarkResult result = driver.run(threads, warmupTimeSeconds, benchTimeSeconds, exceptionHandler, targetClass);
    consumer.accept(result);
    return result;
  }
}
