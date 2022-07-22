package com.obsidiandynamics.dyno;

import com.obsidiandynamics.func.*;

final class BenchmarkSupport {
  private BenchmarkSupport() {}
  
  static BenchmarkTarget resolve(String targetClass) throws Exception {
    final Class<? extends BenchmarkTarget> cls = Classes.cast(Class.forName(targetClass));
    return resolve(cls);
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
