package com.obsidiandynamics.dyno;

import com.obsidiandynamics.resolver.*;

public final class ThreadGroupScopedBenchmarkTarget extends ScopedBenchmarkTarget {
  private static final Scope scope = Scope.THREAD_GROUP;
  
  public static void primeDelegate(BenchmarkTarget delegate) {
    primeDelegate(scope, delegate);
  }
  
  public static void clearDelegate() {
    clearDelegate(scope);
  }

  @Override
  protected Scope getScope() {
    return scope;
  }
}