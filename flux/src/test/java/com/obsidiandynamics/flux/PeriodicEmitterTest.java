package com.obsidiandynamics.flux;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.junit.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.worker.*;

public final class PeriodicEmitterTest {
  @Test
  public void testFlat() {
    final List<Integer> collected = new ArrayList<>();
    final AtomicInteger generator = new AtomicInteger();
    final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);
    final PeriodicEmitter<Integer> emitter = Emitters
        .periodic(Rate.flatBuilder().withRate(1_000).withDuration(.050).build(), generator::getAndIncrement)
        .withWorkerOptions(new WorkerOptions().daemon().withName("CustomThreadName"))
        .onComplete(completionHandler);
    
    final Flux flux = new Flux()
        .cascade(emitter)
        .cascade(Sinks.collection(collected))
        .start();

    flux.joinSilently();
    assertTrue(flux.isComplete());
    assertNull(flux.getError());

    final int expectedEvents = 50;
    assertEquals(expectedEvents, emitter.getEmittedEvents());
    assertEquals(expectedEvents, emitter.getExpectedEvents());
    assertEquals(expectedEvents, generator.get());
    assertEquals(expectedEvents, collected.size());
    verifyOrder(collected);
    assertTrue("elapsedTime=" + emitter.getElapsedTime(), 
               emitter.getElapsedTime() >= emitter.getRate().getDuration());
    verify(completionHandler).onComplete();
  }
  
  @Test
  public void testFlat_multipleEventsPerCycle() {
    final AtomicInteger collected = new AtomicInteger();
    final AtomicInteger generator = new AtomicInteger();
    final PeriodicEmitter<Integer> emitter = Emitters
        .periodic(Rate.flatBuilder().withRate(1_000_000).withDuration(.010).build(), 
                  context -> {
                    while (context.hasRemainingCapacity()) {
                      context.emit(generator.getAndIncrement());
                    }
                  });
    
    final Flux flux = new Flux()
        .cascade(emitter)
        .cascade(Sinks.consumer(__ -> collected.incrementAndGet()))
        .start();

    flux.joinSilently();
    assertTrue(flux.isComplete());
    assertNull(flux.getError());

    final int expectedEvents = 10_000;
    assertEquals(expectedEvents, emitter.getEmittedEvents());
    assertEquals(expectedEvents, generator.get());
    assertEquals(expectedEvents, collected.get());
    assertTrue("elapsedTime=" + emitter.getElapsedTime(), 
               emitter.getElapsedTime() >= emitter.getRate().getDuration());
  }

  @Test
  public void testRampUp() {
    final List<Integer> collected = new ArrayList<>();
    final AtomicInteger generator = new AtomicInteger();
    final PeriodicEmitter<Integer> emitter = Emitters.periodic(Rate.rampUpBuilder()
                                                               .withBaseRate(1_000)
                                                               .withPeakRate(2_000)
                                                               .withRampUpTime(.020)
                                                               .withDuration(0.050)
                                                               .build(), 
                                                               generator::getAndIncrement);
    final Flux flux = new Flux()
        .cascade(emitter)
        .cascade(Sinks.collection(collected))
        .start();

    flux.joinSilently();
    assertTrue(flux.isComplete());
    assertNull(flux.getError());

    final int expectedEvents = 90;
    assertEquals(expectedEvents, emitter.getEmittedEvents());
    assertEquals(expectedEvents, generator.get());
    assertEquals(expectedEvents, collected.size());
    verifyOrder(collected);
    assertTrue("elapsedTime=" + emitter.getElapsedTime(), 
               emitter.getElapsedTime() >= emitter.getRate().getDuration());
  }

  @Test
  public void testUncheckedExceptionInSupplier() {
    final RuntimeException cause = new RuntimeException("Simulated");
    final AtomicInteger emitterInvoked = new AtomicInteger();
    final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);
    
    final Flux flux = new Flux()
        .onError(ExceptionHandler.nop())
        .cascade(Emitters
                 .periodic(Rate.flatBuilder().withRate(1_000).withDuration(Long.MAX_VALUE).build(),
                           () -> {
                             emitterInvoked.incrementAndGet();
                             throw cause;
                           })
                 .onComplete(completionHandler))
        .cascade(Sinks.nop())
        .start();
    flux.joinSilently();
    assertTrue(flux.isComplete());
    assertTrue(flux.isError());
    assertEquals(cause, flux.getError());
    assertEquals(1, emitterInvoked.get());
    verify(completionHandler).onComplete();
  }

  @Test
  public void testCheckedExceptionInSink() {
    final FluxException cause = new FluxException("Simulated");
    final AtomicInteger sinkInvoked = new AtomicInteger();
    final AtomicInteger generator = new AtomicInteger();
    final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);
    
    final Flux flux = new Flux()
        .onError(ExceptionHandler.nop())
        .cascade(Emitters
                 .periodic(Rate.flatBuilder().withRate(1_000).withDuration(Long.MAX_VALUE).build(),
                           generator::getAndIncrement)
                 .onComplete(completionHandler))
        .cascade(Sinks.consumer((__context, __event) -> {
          sinkInvoked.incrementAndGet();
          throw cause;
        }))
        .start();
    flux.joinSilently();
    assertTrue(flux.isComplete());
    assertTrue(flux.isError());
    assertSame(cause, flux.getError());
    assertEquals(1, sinkInvoked.get());
    verify(completionHandler).onComplete();
  }

  @Test
  public void testTerminateStage_withMaxRate() {
    final List<Integer> collected = new ArrayList<>();
    final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);
    
    final Flux flux = new Flux()
        .cascade(Emitters
                 .<Integer>periodic(Rate.flatBuilder().withRate(1_000).withDuration(Long.MAX_VALUE).build(),
                                    StageContext::terminate)
                 .onComplete(completionHandler))
        .cascade(Sinks.collection(collected))
        .start();
    flux.joinSilently();
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    assertEquals(Collections.emptyList(), collected);
    verify(completionHandler).onComplete();
  }
  
  @Test
  public void testTerminatePipeline() {
    final AtomicInteger generator = new AtomicInteger();
    final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);
    final PeriodicEmitter<Integer> emitter = Emitters
        .periodic(Rate.flatBuilder().withRate(1_000).withDuration(Long.MAX_VALUE).build(),
                  generator::getAndIncrement)
        .onComplete(completionHandler);
    
    final Flux flux = new Flux()
        .cascade(emitter)
        .cascade(Sinks.nop())
        .start();

    flux.terminate().joinSilently();
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    verify(completionHandler).onComplete();
  }

  private static void verifyOrder(List<Integer> ints) {
    for (int i = 0; i < ints.size(); i++) {
      assertEquals(i, (int) ints.get(i));
    }
  }
}
