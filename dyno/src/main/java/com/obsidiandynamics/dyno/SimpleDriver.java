package com.obsidiandynamics.dyno;

import java.util.concurrent.*;
import java.util.function.*;

public final class SimpleDriver implements BenchmarkDriver {
  @Override
  public BenchmarkResult run(int threads, int warmupTime, int benchTime, Consumer<Exception> exceptionHandler,
                             Class<? extends BenchmarkTarget> targetClass) {
    final BenchmarkTarget target;
    try {
      target = BenchmarkSupport.resolve(targetClass);
    } catch (Exception e) {
      exceptionHandler.accept(e);
      return null;
    }
    
    try {
      return run(threads, warmupTime, benchTime, exceptionHandler, target);
    } finally {
      try {
        BenchmarkSupport.dispose(target);
      } catch (Exception e) {
        exceptionHandler.accept(e);
      }
    }
  }
  
  /**
   *  Staggered brackets of sampling intervals used as a switch statement constant.<p> 
   *  
   *  The reason for implementing seemingly redundant logic in switch statements is that
   *  it allows the loops to make comparisons against constants, thereby benefiting from
   *  compiler inlining.
   */
  private enum SampleIntervalGroup {
    _1,
    _10,
    _100,
    _1000,
    _10000;
    
    private final int value;
    
    private SampleIntervalGroup() { 
      value = Integer.parseInt(name().substring(1));
    }
    
    static SampleIntervalGroup from(long sampleInterval) {
      for (int i = values().length - 1; i >= 0; i--) {
        if (values()[i].value <= sampleInterval) {
          return values()[i];
        }
      }
      throw new IllegalArgumentException("Invalid sample interval");
    }
  }

  private static class RunnerThread extends Thread {
    private final BenchmarkTarget target;
    private final Consumer<Exception> exceptionHandler;

    private volatile boolean running;
    private volatile boolean terminated;
    private volatile CountDownLatch latch;
    private volatile long ops;
    private volatile SampleIntervalGroup group;
    
    private RunnerThread(BenchmarkTarget target, Consumer<Exception> exceptionHandler, int threadNo) {
      super("RunnerThread-" + target.getClass().getCanonicalName() + "-" + threadNo);
      setDaemon(true);
      this.target = target;
      this.exceptionHandler = exceptionHandler;
      start();
    }
    
    void go(SampleIntervalGroup group) {
      ops = 0;
      this.group = group;
      running = true;
      synchronized (this) {
        notify();
      }
    }
    
    void stop(CountDownLatch latch) {
      this.latch = latch;
      running = false;
    }
    
    void terminate() {
      terminated = true;
      synchronized (this) {
        notify();
      }
    }
    
    @Override
    public void run() {
      try {
        while (true) {
          if (running) {
            inner: while (true) {
              long ops = 0;
              // The only thing different among the case statements is the ops modulo used to determine when
              // the loop should be stopped. By duplicating code and using constants, the benchmark harness is
              // roughly an order of magnitude more efficient, as one of the operands to the mod operator is a 
              // constant, which will be subjected to inlining by the bytecode compiler.
              switch (group) {
                case _1:
                  while (true) {
                    target.call();
                    ops++;
                    if (! running) {
                      this.ops = ops;
                      break inner;
                    }
                  }
                case _10:
                  while (true) {
                    target.call();
                    ops++;
                    if (ops % 10 == 0 && ! running) {
                      this.ops = ops;
                      break inner;
                    }
                  }
                case _100:
                  while (true) {
                    target.call();
                    ops++;
                    if (ops % 100 == 0 && ! running) {
                      this.ops = ops;
                      break inner;
                    }
                  }
                case _1000:
                  while (true) {
                    target.call();
                    ops++;
                    if (ops % 1000 == 0 && ! running) {
                      this.ops = ops;
                      break inner;
                    }
                  }
                case _10000:
                  while (true) {
                    target.call();
                    ops++;
                    if (ops % 10000 == 0 && ! running) {
                      this.ops = ops;
                      break inner;
                    }
                  }
                default:
                  throw new IllegalStateException();
              }
            }
          } else if (terminated) {
            return;
          } else {
            if (latch != null) latch.countDown();
            synchronized (this) {
              wait(1000);
            }
          }
        }
      } catch (Exception e) {
        exceptionHandler.accept(e);
      }
    }
  }
  
  private static BenchmarkResult run(int threads, int warmupTime, int benchTime, Consumer<Exception> exceptionHandler,
                                     BenchmarkTarget target) {
    final RunnerThread[] runners = new RunnerThread[threads];
    for (int threadNo = 0; threadNo < threads; threadNo++) {
      runners[threadNo] = new RunnerThread(target, exceptionHandler, threadNo);
    }
    
    long sampleInterval = 10;
    if (warmupTime != 0) {
      sampleInterval = run(runners, warmupTime, sampleInterval, exceptionHandler);
    }
    final long start = System.nanoTime();
    final long ops = run(runners, benchTime, sampleInterval, exceptionHandler);
    final double duration = (System.nanoTime() - start) / 1_000_000_000d;
    final double rate = ops / duration;
    
    for (RunnerThread runner : runners) {
      runner.terminate();
    }
    return new BenchmarkResult(duration, rate);
  }
  
  private static long run(RunnerThread[] runners, int time, long sampleInterval, Consumer<Exception> exceptionHandler) {
    //System.out.println("sampleInterval=" + sampleInterval + ", group=" + SampleIntervalGroup.from(sampleInterval));
    final SampleIntervalGroup group = SampleIntervalGroup.from(time);
    try {
      for (RunnerThread runner : runners) {
        runner.go(group);
      }

      Thread.sleep(time * 1000);

      final CountDownLatch latch = new CountDownLatch(runners.length);
      for (RunnerThread runner : runners) {
        runner.stop(latch);
      }

      latch.await();
      
      long ops = 0;
      for (RunnerThread runner : runners) {
        ops += runner.ops;
      }
      return ops;
    } catch (InterruptedException e) {
      exceptionHandler.accept(e);
      return 0;
    }
  }
}
