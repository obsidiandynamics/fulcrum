package com.obsidiandynamics.dyno;

final class BenchmarkSupport {
  private BenchmarkSupport() {}
  
  @SuppressWarnings("unchecked")
  static BenchmarkTarget resolve(String targetClass) throws ClassNotFoundException, Exception {
    return resolve((Class<? extends BenchmarkTarget>) Class.forName(targetClass));
  }
  
  static BenchmarkTarget resolve(Class<? extends BenchmarkTarget> targetClass) throws Exception {
    final BenchmarkTarget target = targetClass.getDeclaredConstructor().newInstance();
    target.setup();
    return target;
  }
  
  static void dispose(BenchmarkTarget target) throws Exception {
    target.tearDown();
  }
}
