package com.obsidiandynamics.dyno;

import java.util.*;
import java.util.function.*;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.*;
import org.openjdk.jmh.results.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import com.obsidiandynamics.func.*;

public final class JmhDriver implements BenchmarkDriver {
  private final Consumer<ChainedOptionsBuilder> optionsBuilder;
  
  public JmhDriver() {
    this(__optionsBuilder -> {});
  }
  
  public JmhDriver(Consumer<ChainedOptionsBuilder> optionsBuilderConsumer) {
    this.optionsBuilder = optionsBuilderConsumer;
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
   *  1. Sources will be automatically copied to {@code src/main/java}. <em>Afterwards organise imports to 
   *  remove any IDE warnings.</em><br>
   *  2. Configuration will be copied {@code src/main/resources/META-INF}.
   */
  @State(Scope.Thread)
  public static class JmhWrapper {
    @Param("")
    private String targetClass;
    
    private BenchmarkTarget target;
    
    private final BlackholeAbyss abyss = new BlackholeAbyss();
    
    @Setup
    public void setup() throws Exception {
      target = resolveTarget(targetClass);
    }
    
    @Benchmark
    public void bench(Blackhole blackhole) throws Exception {
      final BlackholeAbyss abyss = this.abyss;
      abyss.blackhole = blackhole;
      runTarget(target, abyss);
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
  
  private static void runTarget(BenchmarkTarget target, Abyss abyss) throws Exception {
    target.cycle(abyss);
  }
  
  private static void disposeTarget(BenchmarkTarget target) throws Exception {
    BenchmarkSupport.dispose(target);
  }
  
  @Override
  public BenchmarkResult run(int threads, 
                             int warmupTimeMillis, 
                             int benchTimeMillis, 
                             ExceptionHandler exceptionHandler,
                             Class<? extends BenchmarkTarget> targetClass) {
    final ChainedOptionsBuilder builder = new OptionsBuilder()
    .include(JmhWrapper.class.getSimpleName())
    .threads(threads)
    .warmupIterations(1)
    .warmupTime(TimeValue.milliseconds(warmupTimeMillis))
    .measurementIterations(5)
    .measurementTime(TimeValue.milliseconds(benchTimeMillis))
    .param("targetClass", targetClass.getName())
    .forks(1);
    
    optionsBuilder.accept(builder);
    
    final Options opts = builder.build();
    
    final Collection<RunResult> results;
    final long started = System.currentTimeMillis();
    try {
      results = new Runner(opts).run();
    } catch (RunnerException e) {
      exceptionHandler.onException("Unexpected exception", e);
      return new BenchmarkResult(0, 0, Collections.emptyList());
    }
    final long took = System.currentTimeMillis() - started;
    
    final Result<?> primary = getFirstPrimaryResult(results);
    return new BenchmarkResult(took, primary.getScore(), results);
  }
  
  private static Result<?> getFirstPrimaryResult(Collection<RunResult> results) {
    return results.iterator().next().getPrimaryResult();
  }
}
