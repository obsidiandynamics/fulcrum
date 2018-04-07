package com.obsidiandynamics.dyno;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.function.*;

import org.junit.*;

import com.obsidiandynamics.func.*;

public final class DynoTest {
  @Test
  public void testConfigAndRun() {
    final BenchmarkDriver driver = mock(BenchmarkDriver.class);
    final BenchmarkResult result = new BenchmarkResult(100, 200, null);
    when(driver.run(anyInt(), anyInt(), anyInt(), any(), any())).thenReturn(result);
    
    final int threads = 4;
    final int benchmarkTime = 100;
    final double warmupFrac = 0.25;
    final ExceptionHandler exceptionHandler = mock(ExceptionHandler.class);
    final Class<BenchmarkTarget> targetClass = BenchmarkTarget.class;
    final Consumer<BenchmarkResult> consumer = Classes.cast(mock(Consumer.class));
    
    final BenchmarkResult returned = new Dyno()
        .withBenchmarkTime(benchmarkTime)
        .withDriver(driver)
        .withExceptionHandler(exceptionHandler)
        .withOutput(consumer)
        .withTarget(targetClass)
        .withThreads(threads)
        .withWarmupFraction(warmupFrac)
        .run();
    
    assertSame(result, returned);
    verify(consumer).accept(eq(result));
    final int warmupTime = (int) (benchmarkTime * warmupFrac);
    verify(driver).run(eq(threads), eq(warmupTime), eq(benchmarkTime), eq(exceptionHandler), eq(targetClass));
    verifyNoMoreInteractions(exceptionHandler);
  }
  
  @Test
  public void testDefaultResultConsumerCoverage() {
    Dyno.defaultResultConsumer.accept(null);
  }
}
