package com.obsidiandynamics.dyno;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class BenchmarkSupportTest {
  @After
  public void after() {
    ThreadScopedBenchmarkTarget.clearDelegate();
  }
  
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(BenchmarkSupport.class);
  }

  @Test
  public void testLifecycle() throws Exception {
    final BenchmarkTarget delegate = mock(BenchmarkTarget.class);
    ThreadScopedBenchmarkTarget.primeDelegate(delegate);
    
    final BenchmarkTarget benchmark = BenchmarkSupport.resolve(ThreadScopedBenchmarkTarget.class.getName());
    verify(delegate).setup();
    
    final Abyss abyss = mock(Abyss.class);
    benchmark.cycle(abyss);
    verify(delegate).cycle(eq(abyss));
    
    BenchmarkSupport.dispose(benchmark);
    verify(delegate).tearDown();
  }
}
