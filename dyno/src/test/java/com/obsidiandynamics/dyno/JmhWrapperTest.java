package com.obsidiandynamics.dyno;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.*;
import org.openjdk.jmh.infra.*;

import com.obsidiandynamics.dyno.JmhDriver.*;

public final class JmhWrapperTest {
  @After
  public void after() {
    ThreadScopedBenchmarkTarget.clearDelegate();
  }
  
  @Test
  public void testLifecycle() throws Exception {
    final BenchmarkTarget delegate = mock(BenchmarkTarget.class);
    ThreadScopedBenchmarkTarget.primeDelegate(delegate);
    
    final JmhWrapper wrapper = new JmhWrapper();
    JmhDriver.assignTargetClass(wrapper, ThreadScopedBenchmarkTarget.class.getName());
    wrapper.setup();
    verify(delegate).setup();
    
    final Blackhole blackhole = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
    wrapper.bench(blackhole);
    verify(delegate).cycle(isA(BlackholeAbyss.class));
    
    wrapper.tearDown();
    verify(delegate).tearDown();
  }
}
