package com.obsidiandynamics.dyno;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.options.*;

public final class JmhDriverSample implements BenchmarkTarget {
  @Override
  public void setup() {
    // optional setup code
  }
  
  @Override
  public void tearDown() {
    // optional tear-down code
  }
  
  @Override
  public void cycle(Abyss abyss) throws Exception {
    abyss.consume(Math.log(Math.exp(1)));
  }
  
  public static void main(String[] args) {
    new Dyno()
    .withBenchmarkTime(1_000)
    .withTarget(JmhDriverSample.class)
    .withThreads(2)
    .withDriver(new JmhDriver(opts -> opts
                              .mode(Mode.Throughput)
                              .verbosity(VerboseMode.EXTRA)
                              .forks(1)
                              .shouldDoGC(true)
                              .measurementIterations(2)))
    .withWarmupFraction(0.1)
    .withOutput(System.out::println)
    .run();
  }
}
