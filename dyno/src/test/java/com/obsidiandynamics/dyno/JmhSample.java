package com.obsidiandynamics.dyno;

import org.openjdk.jmh.annotations.*;

public class JmhSample implements BenchmarkTarget {
  @Override
  public void cycle(Abyss abyss) throws Exception {
    abyss.consume(Math.log(Math.exp(1)));
  }
  
  public static void main(String[] args) {
    new Dyno()
    .withBenchTime(5_000)
    .withTarget(JmhSample.class)
    .withDriver(new JmhDriver(opts -> opts
                              .mode(Mode.Throughput)
                              .measurementIterations(2)))
    .withWarmupFraction(0.1)
    .withOutput(System.out::println)
    .run();
  }
}
