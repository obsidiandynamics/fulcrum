package com.obsidiandynamics.dyno;

import java.util.*;
import java.util.function.*;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

public final class JmhDriver implements BenchmarkDriver {
  private final Consumer<ChainedOptionsBuilder> optionsBuilder;
  
  public JmhDriver() {
    this($ -> {});
  }
  
  public JmhDriver(Consumer<ChainedOptionsBuilder> optionsBuilder) {
    this.optionsBuilder = optionsBuilder;
  }
  
  /**
   *  This wrapper is deliberately lightweight to minimise the need for regenerating the JMH benchmark
   *  sources using the annotation processor. Regeneration of code would only be required in the following
   *  scenarios:<br>
   *  1. The class definition of {@link JmhWrapper} is altered.<br>
   *  2. A new version of the JMH library is adopted.<br>
   *  3. An incompatible version of the JVM is used.<p>
   *  
   *  To regenerate run {@code gradle gen}.<br>
   *  1. Sources will be automatically copied to {@code src/gen/java}. <em>Afterwards organise imports to 
   *  remove any IDE warnings.</em><br>
   *  2. Configuration will be copied {@code src/gen/resources/META-INF}.
   */
  @State(Scope.Benchmark)
  public static class JmhWrapper {
    @Param("")
    private String targetClass;
    
    private BenchmarkTarget target;
    
    @Setup
    public void setup() throws Exception {
      target = resolveTarget(targetClass);
    }
    
    @Benchmark
    public Object bench() throws Exception {
      return runTarget(target);
    }
    
    @TearDown
    public void tearDown() throws Exception {
      disposeTarget(target);
    }
  }
  
  private static BenchmarkTarget resolveTarget(String targetClass) throws Exception {
    if (targetClass.isEmpty()) {
      throw new AssertionError("Target class not set");
    }
    return BenchmarkSupport.resolve(targetClass);
  }
  
  private static Object runTarget(BenchmarkTarget target) throws Exception {
    return target.call();
  }
  
  private static void disposeTarget(BenchmarkTarget target) throws Exception {
    BenchmarkSupport.dispose(target);
  }
  
  @Override
  public BenchmarkResult run(int threads, int warmupTime, int benchTime, Consumer<Exception> exceptionHandler,
                             Class<? extends BenchmarkTarget> targetClass) {
    final ChainedOptionsBuilder builder = new OptionsBuilder()
    .include(JmhWrapper.class.getSimpleName())
    .threads(threads)
    .warmupIterations(1)
    .warmupTime(TimeValue.seconds(warmupTime))
    .measurementIterations(5)
    .measurementTime(TimeValue.seconds(benchTime))
    .param("targetClass", targetClass.getName())
    .forks(1);
    
    optionsBuilder.accept(builder);
    
    final Options opts = builder.build();
    
    final Collection<RunResult> results;
    final long started = System.currentTimeMillis();
    try {
      results = new Runner(opts).run();
    } catch (RunnerException e) {
      exceptionHandler.accept(e);
      return new BenchmarkResult(0, 0);
    }
    final long took = System.currentTimeMillis() - started;
    
    if (results.size() != 1) throw new AssertionError("Expecting exactly one result, got " + results.size());
    
    final Result<?> primary = results.iterator().next().getPrimaryResult();
    return new BenchmarkResult(took / 1000d, primary.getScore());
  }
}
