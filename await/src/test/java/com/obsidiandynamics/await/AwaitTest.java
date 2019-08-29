package com.obsidiandynamics.await;

import static com.obsidiandynamics.await.Await.*;
import static junit.framework.TestCase.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.*;
import java.util.function.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class AwaitTest {
  private static final long SOME_TIME_AGO = System.currentTimeMillis() - 10_000;
  
  @After
  public void after() {
    assertFalse(Thread.interrupted());
  }
  
  @Test
  public void testPerpetual_interruptedDuringSleep() throws InterruptedException, TimeoutException {
    Thread.currentThread().interrupt();
    assertThatThrownBy(() -> {
      Await.perpetual(() -> false);
    }).isExactlyInstanceOf(InterruptedException.class);
  }
  
  @Test
  public void testPerpetual_interruptedAfterPass() throws InterruptedException, TimeoutException {
    assertThatThrownBy(() -> {
      Await.perpetual(() -> {
        Thread.currentThread().interrupt();
        return true;
      });
    }).isExactlyInstanceOf(InterruptedException.class).hasMessage("Wait interrupted");
  }
  
  @Test
  public void testBounded_timeout() throws InterruptedException {
    final long start = System.currentTimeMillis();
    final boolean r = Await.bounded(20, () -> false);
    assertFalse(r);
    final long elapsed = System.currentTimeMillis() - start;
    assertTrue("Elapsed " + elapsed, elapsed >= 20);
  }
  
  @Test
  public void testBounded_passed() throws InterruptedException {
    final boolean r = Await.bounded(20, () -> true);
    assertTrue(r);
  }
  
  @Test
  public void testBounded_zeroMillisTimeout() throws InterruptedException {
    final long start = System.currentTimeMillis();
    final boolean r = Await.bounded(20, 0, () -> false);
    assertFalse(r);
    final long elapsed = System.currentTimeMillis() - start;
    assertTrue("Elapsed " + elapsed, elapsed >= 0);
  }

  @Test
  public void testPerpetual_conditionPassed() throws InterruptedException {
    Await.perpetual(() -> true);
    Await.perpetual(10, () -> true);
  }
  
  @Test
  public void testBoundedTimeout_atLeastOnce_timeout() throws InterruptedException, TimeoutException {
    final BooleanSupplier condition = mock(BooleanSupplier.class);
    assertThatThrownBy(() -> {
      Await.boundedTimeout(SOME_TIME_AGO, 0, 1, AT_LEAST_ONCE, condition);
    }).isExactlyInstanceOf(TimeoutException.class);
    verify(condition).getAsBoolean();
  }
  
  @Test
  public void testBoundedTimeout_possiblyNever_timeout() throws InterruptedException, TimeoutException {
    final BooleanSupplier condition = mock(BooleanSupplier.class);
    assertThatThrownBy(() -> {
      Await.boundedTimeout(SOME_TIME_AGO, 0, 1, POSSIBLY_NEVER, condition);
    }).isExactlyInstanceOf(TimeoutException.class);
    verify(condition, never()).getAsBoolean();
  }
  
  @Test
  public void testBoundedTimeout_atLeastOnce_passed() throws InterruptedException, TimeoutException {
    final BooleanSupplier condition = mock(BooleanSupplier.class);
    when(condition.getAsBoolean()).thenReturn(true);
    Await.boundedTimeout(SOME_TIME_AGO, 0, 1, AT_LEAST_ONCE, condition);
    verify(condition).getAsBoolean();
  }
  
  @Test
  public void testBoundedTimeout_possiblyNever_passed() throws InterruptedException, TimeoutException {
    final BooleanSupplier condition = mock(BooleanSupplier.class);
    when(condition.getAsBoolean()).thenReturn(true);
    Await.boundedTimeout(System.currentTimeMillis(), 10_000, 1, POSSIBLY_NEVER, condition);
    verify(condition).getAsBoolean();
  }
  
  @Test
  public void testBoundedTimeout_passed() throws InterruptedException, TimeoutException {
    final BooleanSupplier condition = mock(BooleanSupplier.class);
    when(condition.getAsBoolean()).thenReturn(true);
    Await.boundedTimeout(20, condition);
    verify(condition).getAsBoolean();
  }

  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Await.class);
  }
}
