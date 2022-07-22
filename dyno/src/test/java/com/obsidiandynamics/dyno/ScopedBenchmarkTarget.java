package com.obsidiandynamics.dyno;

import static org.junit.Assert.*;

import com.obsidiandynamics.resolver.*;

public abstract class ScopedBenchmarkTarget implements BenchmarkTarget {
  private final BenchmarkTarget delegate = Resolver.scope(getScope()).lookup(BenchmarkTarget.class).get();
  
  protected abstract Scope getScope();
  
  @Override
  public final void setup() throws Exception {
    assertNotNull(delegate);
    delegate.setup();
  }
  
  @Override
  public final void tearDown() throws Exception {
    assertNotNull(delegate);
    delegate.tearDown();
  }
  
  @Override
  public final void cycle(Abyss abyss) throws Exception {
    assertNotNull(delegate);
    delegate.cycle(abyss);
  }
  
  protected static void primeDelegate(Scope scope, BenchmarkTarget delegate) {
    Resolver.scope(scope).assign(BenchmarkTarget.class, Singleton.of(delegate));
  }
  
  protected static void clearDelegate(Scope scope) {
    Resolver.scope(scope).reset(BenchmarkTarget.class);
  }
}