package com.obsidiandynamics.dyno;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.function.*;

import org.junit.*;

import com.obsidiandynamics.func.*;

public final class SimpleDriverTest {
  @Test
  public void testCalibrateBatchSize() {
    assertEquals(10_000, SimpleDriver.calibrateBatchSize(1_000, 11));
    assertEquals(10_000, SimpleDriver.calibrateBatchSize(1_000, 10));
    assertEquals(9_000, SimpleDriver.calibrateBatchSize(1_000, 9));

    assertEquals(9, SimpleDriver.calibrateBatchSize(1, 9));
    assertEquals(1, SimpleDriver.calibrateBatchSize(0.1, 9));
  }
  
  @Test
  public void testLogSilent() {
    final Consumer<String> logPrinter = Classes.cast(mock(Consumer.class));
    final SimpleDriver d = new SimpleDriver().withLogPrinter(logPrinter);
    d.log("format");
    verifyNoMoreInteractions(logPrinter);
  }
  
  @Test
  public void testLogVerbose() {
    final Consumer<String> logPrinter = Classes.cast(mock(Consumer.class));
    final SimpleDriver d = new SimpleDriver().withVerbose(true).withLogPrinter(logPrinter);
    d.log("format");
    verify(logPrinter).accept(isNotNull());
  }
  
  @Test
  public void testRunWithoutWarmup() throws Exception {
    final BenchmarkTarget delegate = mock(BenchmarkTarget.class);
    ThreadGroupScopedBenchmarkTarget.primeDelegate(delegate);
    final int benchmarkTimeMillis = 10;
    
    final int threads = 2;
    final BenchmarkResult result = new SimpleDriver().run(threads, 0, benchmarkTimeMillis, ThreadGroupScopedBenchmarkTarget.class);
    assertTrue("result.duration=" + result.getDuration(), result.getDuration() >= benchmarkTimeMillis);
    assertTrue("result.score=" + result.getScore(), result.getScore() > 0);
    
    verify(delegate, times(threads)).setup();
    verify(delegate, atLeast(threads)).cycle(isA(BlackholeAbyss.class));
    verify(delegate, times(threads)).tearDown();
  }
  
  @Test
  public void testRunWithWarmup() throws Exception {
    final BenchmarkTarget delegate = mock(BenchmarkTarget.class);
    ThreadGroupScopedBenchmarkTarget.primeDelegate(delegate);
    final int warmupTimeMillis = 5;
    final int benchmarkTimeMillis = 20;

    final int threads = 2;
    final BenchmarkResult result = new SimpleDriver().run(threads, warmupTimeMillis, benchmarkTimeMillis, ThreadGroupScopedBenchmarkTarget.class);
    assertTrue("result.duration=" + result.getDuration(), result.getDuration() >= benchmarkTimeMillis);
    assertTrue("result.score=" + result.getScore(), result.getScore() > 0);
    
    verify(delegate, times(threads + 1)).setup();
    verify(delegate, atLeast(threads + 1)).cycle(isA(BlackholeAbyss.class));
    verify(delegate, times(threads + 1)).tearDown();
  }
  
  @Test(expected=BenchmarkError.class)
  public void testWithException() throws Exception {
    final BenchmarkTarget delegate = mock(BenchmarkTarget.class);
    doThrow(new RuntimeException("simulated error")).when(delegate).cycle(any());
    ThreadGroupScopedBenchmarkTarget.primeDelegate(delegate);
    
    new SimpleDriver().run(1, 0, 10, ThreadGroupScopedBenchmarkTarget.class);
  }
}
