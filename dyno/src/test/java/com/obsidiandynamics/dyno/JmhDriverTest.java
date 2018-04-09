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

public final class JmhDriverTest {
  @Test
  public void testDefaultOptionsBuilderConsumerCoverage() {
    JmhDriver.defaultOptionsBuilderConsumer.accept(null);
  }
  
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

  /**
   *  Note: Running JMH will result in an illegal reflective access, which has been promoted
   *  to a warning in Java 9 and later, and will show up in the build log.
   */
  private static final boolean TEST_REAL_JMH = true;

  @Test
  public void testRunWithoutFork() throws Exception {
    Assume.assumeTrue(TEST_REAL_JMH);
    
    final AtomicReference<Exception> error = new AtomicReference<>();
    final ThreadGroup group = new ThreadGroup(UUID.randomUUID().toString());
    group.setDaemon(true);
    final Thread thread = new Thread(group, () -> {
      final BenchmarkTarget delegate = mock(BenchmarkTarget.class);
      ThreadGroupScopedBenchmarkTarget.primeDelegate(delegate);
      try {
        final int threads = 2;
        final BenchmarkResult result = new JmhDriver(opts -> opts
                                                     .threads(threads)
                                                     .forks(0)
                                                     .verbosity(VerboseMode.NORMAL)
                                                     .warmupIterations(0)
                                                     .measurementIterations(1))
        .run(1, 0, 10, ThreadGroupScopedBenchmarkTarget.class);
        try {
          verify(delegate, times(threads)).setup();
          verify(delegate, atLeast(threads)).cycle(isA(BlackholeAbyss.class));
          verify(delegate, times(threads)).tearDown();
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
  
  public static final class NopBenchmarkTarget implements BenchmarkTarget {
    @Override
    public void cycle(Abyss abyss) throws Exception {}
  }

  @Test
  public void testRunWithFork() throws Exception {
    Assume.assumeTrue(TEST_REAL_JMH);
    
    // When running from an older (JDK 1.8) build using Gradle wrapper, the forking of a JVM will 
    // otherwise fail due to Gradle's security manager (only seems to be installed when running from the wrapper);
    // override Gradle's security manager with a permissive alternative to avoid failure.
    final String securityManagerJvmArg = "-Djava.security.manager=" + PermissiveSecurityManager.class.getName();
    
    final BenchmarkResult result = new JmhDriver(opts -> opts
                                                 .jvmArgsAppend(securityManagerJvmArg)
                                                 .forks(1)
                                                 .verbosity(VerboseMode.NORMAL)
                                                 .warmupIterations(0)
                                                 .measurementIterations(1))
    .run(1, 0, 10, NopBenchmarkTarget.class);
    assertTrue("result.duration=" + result.getDuration(), result.getDuration() > 0);
    assertTrue("result.duration=" + result.getScore(), result.getScore() > 0);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testAveragePrimaryScoreEmpty() {
    JmhDriver.getAveragePrimaryScore(Collections.emptyList());
  }
}
