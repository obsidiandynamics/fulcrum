package com.obsidiandynamics.dyno;

import java.util.concurrent.*;

import org.openjdk.jmh.infra.*;

import com.obsidiandynamics.func.*;

public final class SimpleDriver implements BenchmarkDriver {
  @Override
  public BenchmarkResult run(int threads, 
                             int warmupTimeMillis, 
                             int benchTimeMillis, 
                             ExceptionHandler exceptionHandler,
                             Class<? extends BenchmarkTarget> targetClass) {
    final BenchmarkTarget target;
    try {
      target = BenchmarkSupport.resolve(targetClass);
    } catch (Exception e) {
      exceptionHandler.onException("Error resolving class", e);
      return null;
    }
    
    try {
      return run(threads, warmupTimeMillis, benchTimeMillis, exceptionHandler, target);
    } finally {
      try {
        BenchmarkSupport.dispose(target);
      } catch (Exception e) {
        exceptionHandler.onException("Error disposing benchmark taget", e);
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
    private final ExceptionHandler exceptionHandler;

    private volatile boolean running;
    private volatile boolean terminated;
    private volatile CountDownLatch latch;
    private volatile long ops;
    private volatile SampleIntervalGroup group;
    
    private RunnerThread(BenchmarkTarget target, ExceptionHandler exceptionHandler, int threadNo) {
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
      final BlackholeAbyss abyss = new BlackholeAbyss();
      abyss.blackhole = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
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
                    target.cycle(abyss);
                    ops++;
                    if (! running) {
                      this.ops = ops;
                      break inner;
                    }
                  }
                case _10:
                  while (true) {
                    target.cycle(abyss);
                    ops++;
                    if (ops % 10 == 0 && ! running) {
                      this.ops = ops;
                      break inner;
                    }
                  }
                case _100:
                  while (true) {
                    target.cycle(abyss);
                    ops++;
                    if (ops % 100 == 0 && ! running) {
                      this.ops = ops;
                      break inner;
                    }
                  }
                case _1000:
                  while (true) {
                    target.cycle(abyss);
                    ops++;
                    if (ops % 1000 == 0 && ! running) {
                      this.ops = ops;
                      break inner;
                    }
                  }
                case _10000:
                  while (true) {
                    target.cycle(abyss);
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
        exceptionHandler.onException("Error running benchmark", e);
      }
    }
  }
  
  private static BenchmarkResult run(int threads, 
                                     int warmupTimeSeconds, 
                                     int benchTimeSeconds, 
                                     ExceptionHandler exceptionHandler,
                                     BenchmarkTarget target) {
    final RunnerThread[] runners = new RunnerThread[threads];
    for (int threadNo = 0; threadNo < threads; threadNo++) {
      runners[threadNo] = new RunnerThread(target, exceptionHandler, threadNo);
    }
    
    long sampleInterval = 10;
    if (warmupTimeSeconds != 0) {
      sampleInterval = run(runners, warmupTimeSeconds, sampleInterval, exceptionHandler);
    }
    final long start = System.nanoTime();
    final long ops = run(runners, benchTimeSeconds, sampleInterval, exceptionHandler);
    final long durationMillis = (System.nanoTime() - start) / 1_000_000L;
    final double rate = ops / durationMillis * 1000d;
    
    for (RunnerThread runner : runners) {
      runner.terminate();
    }
    return new BenchmarkResult(durationMillis, rate, null);
  }
  
  private static long run(RunnerThread[] runners, int timeSeconds, long sampleInterval, ExceptionHandler exceptionHandler) {
    final SampleIntervalGroup group = SampleIntervalGroup.from(sampleInterval);
    try {
      for (RunnerThread runner : runners) {
        runner.go(group);
      }

      Thread.sleep(timeSeconds * 1000);

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
      exceptionHandler.onException("Interrupted", e);
      return 0;
    }
  }
}
