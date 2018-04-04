package com.obsidiandynamics.dyno;

public class MicrobenchSample implements BenchmarkTarget {
  @Override
  public Object call() throws Exception {
    return Math.log(Math.exp(1));
  }
  
  public static void main(String[] args) {
    new Microbench()
    .time(5)
    .target(MicrobenchSample.class)
    .output(System.out::println)
    .run();
  }
}
