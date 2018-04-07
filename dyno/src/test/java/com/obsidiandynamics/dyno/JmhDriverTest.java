package com.obsidiandynamics.dyno;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.junit.*;
import org.openjdk.jmh.runner.options.*;
import org.openjdk.jmh.util.Optional;

import com.obsidiandynamics.func.*;

public final class JmhDriverTest {
  @Test
  public void testBuildOptions() {
    final Options options = JmhDriver.buildOptions(2, 100, 200, BenchmarkTarget.class, opts -> opts.forks(4));
    assertEquals(Optional.of(2), options.getThreads());
    assertEquals(Optional.of(new TimeValue(100, TimeUnit.MILLISECONDS)), options.getWarmupTime());
    assertEquals(Optional.of(new TimeValue(200, TimeUnit.MILLISECONDS)), options.getMeasurementTime());
    final Collection<String> params = options.getParameter("targetClass").get();
    assertEquals(1, params.size());
    assertEquals(BenchmarkTarget.class.getName(), params.iterator().next());
    assertEquals(Optional.of(4), options.getForkCount());
  }

  @Test
  public void testRun() throws Exception {
    final AtomicReference<Exception> error = new AtomicReference<>();
    final ThreadGroup group = new ThreadGroup(UUID.randomUUID().toString());
    final Thread thread = new Thread(group, () -> {
      final BenchmarkTarget delegate = mock(BenchmarkTarget.class);
      ThreadGroupScopedBenchmarkTarget.primeDelegate(delegate);
      try {
        final ExceptionHandler exceptionHandler = mock(ExceptionHandler.class);
        final BenchmarkResult result = new JmhDriver(opts -> opts
                                                     .forks(0)
                                                     .verbosity(VerboseMode.NORMAL)
                                                     .warmupIterations(0)
                                                     .measurementIterations(1))
        .run(1, 0, 10, exceptionHandler, ThreadGroupScopedBenchmarkTarget.class);
        verifyNoMoreInteractions(exceptionHandler);
        try {
          verify(delegate).setup();
          verify(delegate, atLeastOnce()).cycle(isA(BlackholeAbyss.class));
          verify(delegate).tearDown();
        } catch (Exception e) {
          error.set(e);
          e.printStackTrace();
        }
        assertNotNull(result);
        assertTrue("result.duration=" + result.getDuration(), result.getDuration() > 0);
        assertTrue("result.duration=" + result.getScore(), result.getScore() > 0);
      } finally {
        ThreadGroupScopedBenchmarkTarget.clearDelegate();
      }
    });
    thread.start();
    thread.join();
    assertNull(error.get());
  }
}
