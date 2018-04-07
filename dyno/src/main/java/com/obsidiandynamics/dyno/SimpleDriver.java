package com.obsidiandynamics.dyno;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.openjdk.jmh.infra.*;

import com.obsidiandynamics.func.*;

public final class SimpleDriver implements BenchmarkDriver {
  private boolean verbose;
  
  private Consumer<String> logPrinter = System.out::print;
  
  public SimpleDriver withVerbose(boolean verbose) {
    this.verbose = verbose;
    return this;
  }
  
  public SimpleDriver withLogPrinter(Consumer<String> logPrinter) {
    this.logPrinter = logPrinter;
    return this;
  }
  
  void log(String format, Object... args) {
    if (verbose) {
      logPrinter.accept(String.format(format, args));
    }
  }
  
  @Override
  public BenchmarkResult run(int threads, 
                             int warmupTimeMillis, 
                             int benchmarkTimeMillis, 
                             Class<? extends BenchmarkTarget> targetClass) {
    return Exceptions.wrap(() -> {
      int batchSize = 1_000;
      if (warmupTimeMillis != 0) {
        log("# Warming up... ");
        final SimpleRunner r = new SimpleRunner(batchSize, warmupTimeMillis, targetClass, new CyclicBarrier(1));
        r.join();
        final double warmupRate = (double) r.cycles / r.tookMillis;
        log("done in %,d ms\n", r.tookMillis);
        batchSize = calibrateBatchSize(warmupRate, benchmarkTimeMillis);
        log("# Warmup rate: %,.3f cycles/sec\n", warmupRate * 1_000);
        log("# Recalibrated batch size to %,d cycles\n", batchSize);
      }

      log("Starting timed run... ");
      final List<SimpleRunner> runners = new ArrayList<>(threads);
      final CyclicBarrier barrier = new CyclicBarrier(threads);
      final long start = System.currentTimeMillis();
      for (int i = 0; i < threads; i++) {
        runners.add(new SimpleRunner(batchSize, benchmarkTimeMillis, targetClass, barrier));
      }
      
      // wait for all the runners to finish
      for (SimpleRunner runner : runners) {
        runner.join();
      }
      log("done in %,d ms\n", System.currentTimeMillis() - start);

      // if any of the runners throw an error, rethrow that error here
      final Throwable error = runners.stream().map(r -> r.error.get()).filter(e -> e != null).findFirst().orElse(null);
      if (error != null) {
        throw new BenchmarkError(error);
      }
      
      final double averageTimeMillis = runners.stream().mapToLong(r -> r.runTimeMillis).average().getAsDouble();
      final long totalCycles = runners.stream().mapToLong(r -> r.cycles).sum();
      final double rate = totalCycles / averageTimeMillis * 1_000d;
      log("Measured rate: %,.3f cycles/sec (%,.3f ns/cycle)\n", rate, 1_000_000_000d / rate);
      
      return new BenchmarkResult((long) averageTimeMillis, rate, null);
    }, BenchmarkError::new);
  }
  
  /**
   *  Calibrates a batch size so that a check is performed at most approximately once every 10 millis or
   *  {@code benchmarkTimeMillis}, whichever is lower.
   *  
   *  @param cyclesPerMillisecond
   *  @param benchmarkTimeMillis
   *  @return
   */
  static int calibrateBatchSize(double cyclesPerMillisecond, int benchmarkTimeMillis) {
    final int checkIntervalMillis = Math.min(benchmarkTimeMillis, 10);
    return Math.max((int) (cyclesPerMillisecond * checkIntervalMillis), 1);
  }
  
  private static class SimpleRunner extends Thread {
    private final int batchSize;
    
    private final int runTimeMillis;
    
    private final Class<? extends BenchmarkTarget> targetClass;
    
    private final CyclicBarrier barrier;
    
    private final AtomicReference<Throwable> error = new AtomicReference<>();
    
    private long cycles;
    
    private long tookMillis;
    
    SimpleRunner(int batchSize, int runTimeMillis, Class<? extends BenchmarkTarget> targetClass, CyclicBarrier barrier) {
      super(SimpleRunner.class.getSimpleName());
      this.batchSize = batchSize;
      this.runTimeMillis = runTimeMillis;
      this.targetClass = targetClass;
      this.barrier = barrier;
      start();
    }

    @Override
    public void run() {
      try {
        final BenchmarkTarget target = BenchmarkSupport.resolve(targetClass);
        final BlackholeAbyss abyss = new BlackholeAbyss();
        abyss.blackhole = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
        final long runTimeNanos = runTimeMillis * 1_000_000L;
        final int batchSize = this.batchSize;
        
        barrier.await();
        
        final long start = System.nanoTime();
        long cycles = 0;
        try {
          for (;;) {
            for (int i = 0; i < batchSize; i++) {
              target.cycle(abyss);
            }
            
            final long tookNanos = System.nanoTime() - start;
            cycles += batchSize;
            if (tookNanos >= runTimeNanos) {
              this.cycles = cycles;
              this.tookMillis = tookNanos / 1_000_000L;
              return;
            }
          }
        } finally {
          BenchmarkSupport.dispose(target);
        }
      } catch (Throwable e) {
        error.set(e);
      }
    }
  }
}
