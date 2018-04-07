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
  static final Consumer<ChainedOptionsBuilder> defaultOptionsBuilderConsumer = __ -> {};
  
  private final Consumer<ChainedOptionsBuilder> optionsBuilderConsumer;

  public JmhDriver() {
    this(defaultOptionsBuilderConsumer);
  }

  public JmhDriver(Consumer<ChainedOptionsBuilder> optionsBuilderConsumer) {
    this.optionsBuilderConsumer = optionsBuilderConsumer;
  }

  /**
   *  This wrapper is deliberately lightweight to minimise the need for regenerating the JMH benchmark
   *  sources using the annotation processor. Regeneration of code would only be required in the following
   *  scenarios:<br>
   *  1. The class definition of {@link JmhWrapper} is altered.<br>
   *  2. A new version of the JMH library is adopted.<p>
   *  
   *  To regenerate run {@code gradle gen}.<br>
   *  1. Sources will be automatically copied to {@code src/main/java}, into the {@code c.o.d.generated}
   *  package. <em>Afterwards organise imports to remove any IDE warnings.</em><br>
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
  
  /**
   *  Purely for testing purposes. Directly assigns {@link JmhWrapper#targetClass} to
   *  avoid having to use reflection.
   *  
   *  @param wrapper
   *  @param targetClass
   */
  static void assignTargetClass(JmhWrapper wrapper, String targetClass) {
    wrapper.targetClass = targetClass;
  }

  private static BenchmarkTarget resolveTarget(String targetClass) throws Exception {
    return BenchmarkSupport.resolve(targetClass);
  }

  private static void runTarget(BenchmarkTarget target, Abyss abyss) throws Exception {
    target.cycle(abyss);
  }

  private static void disposeTarget(BenchmarkTarget target) throws Exception {
    BenchmarkSupport.dispose(target);
  }

  static Options buildOptions(int threads, 
                              int warmupTimeMillis, 
                              int benchmarkTimeMillis, 
                              Class<? extends BenchmarkTarget> targetClass,
                              Consumer<ChainedOptionsBuilder> optionsBuilderConsumer) {
    final ChainedOptionsBuilder builder = new OptionsBuilder()
        .include(JmhWrapper.class.getSimpleName())
        .threads(threads)
        .warmupIterations(1)
        .warmupTime(TimeValue.milliseconds(warmupTimeMillis))
        .measurementIterations(5)
        .measurementTime(TimeValue.milliseconds(benchmarkTimeMillis))
        .param("targetClass", targetClass.getName())
        .forks(1);

    optionsBuilderConsumer.accept(builder);

    return builder.build();
  }

  @Override
  public BenchmarkResult run(int threads, 
                             int warmupTimeMillis, 
                             int benchmarkTimeMillis, 
                             ExceptionHandler exceptionHandler,
                             Class<? extends BenchmarkTarget> targetClass) {
    final Options opts = buildOptions(threads, warmupTimeMillis, benchmarkTimeMillis,
                                      targetClass, optionsBuilderConsumer);

    final long started = System.currentTimeMillis();
    final Collection<RunResult> results = Exceptions.wrap(() -> new Runner(opts).run(), BenchmarkException::new);
    final long took = System.currentTimeMillis() - started;

    final Result<?> primary = getFirstPrimaryResult(results);
    return new BenchmarkResult(took, primary.getScore(), results);
  }

  private static Result<?> getFirstPrimaryResult(Collection<RunResult> results) {
    return results.iterator().next().getPrimaryResult();
  }
}
