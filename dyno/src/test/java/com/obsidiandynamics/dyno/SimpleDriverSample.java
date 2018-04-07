package com.obsidiandynamics.dyno;

public final class SimpleDriverSample implements BenchmarkTarget {
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
    .withTarget(SimpleDriverSample.class)
    .withThreads(2)
    .withDriver(new SimpleDriver().withVerbose(true))
    .withWarmupFraction(0.2)
    .withOutput(System.out::println)
    .run();
  }
}
