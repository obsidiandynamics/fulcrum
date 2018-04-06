package com.obsidiandynamics.dyno;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;
import com.obsidiandynamics.resolver.*;

public final class BenchmarkSupportTest {
  public static final class TestBenchmarkTarget implements BenchmarkTarget {
    private final BenchmarkTarget delegate = Resolver.lookup(BenchmarkTarget.class).get();
    
    @Override
    public void setup() throws Exception {
      assertNotNull(delegate);
      delegate.setup();
    }
    
    @Override
    public void tearDown() throws Exception {
      assertNotNull(delegate);
      delegate.tearDown();
    }
    
    @Override
    public void cycle(Abyss abyss) throws Exception {
      assertNotNull(delegate);
      delegate.cycle(abyss);
    }
  }
  
  @After
  public void after() {
    Resolver.reset();
  }
  
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(BenchmarkSupport.class);
  }

  @Test
  public void testLifecycle() throws ClassNotFoundException, Exception {
    final BenchmarkTarget delegate = mock(BenchmarkTarget.class);
    Resolver.assign(BenchmarkTarget.class, () -> delegate);
    
    final BenchmarkTarget benchmark = BenchmarkSupport.resolve(TestBenchmarkTarget.class.getName());
    verify(delegate).setup();
    
    final Abyss abyss = mock(Abyss.class);
    benchmark.cycle(abyss);
    verify(delegate).cycle(eq(abyss));
    
    BenchmarkSupport.dispose(benchmark);
    verify(delegate).tearDown();
  }
}
