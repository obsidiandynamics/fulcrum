package com.obsidiandynamics.flux;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

import org.junit.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.worker.*;

public final class SupplierEmitterTest {
  @Test
  public void testUncheckedExceptionInSupplier() {
    final RuntimeException cause = new RuntimeException("Simulated");
    final AtomicInteger emitterInvoked = new AtomicInteger();
    final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);
    
    final Flux flux = new Flux()
        .onError(ExceptionHandler.nop())
        .cascade(Emitters
                 .supplier(() -> {
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
  public void testUncheckedExceptionInSink() {
    final RuntimeException cause = new RuntimeException("Simulated");
    final AtomicInteger sinkInvoked = new AtomicInteger();
    final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);
    
    final Flux flux = new Flux()
        .onError(ExceptionHandler.nop())
        .cascade(Emitters.array(0, 1, 2).onComplete(completionHandler))
        .cascade(Sinks.consumer(__ -> {
          sinkInvoked.incrementAndGet();
          throw cause;
        }))
        .start();
    flux.joinSilently();
    assertTrue(flux.isComplete());
    assertTrue(flux.isError());
    assertEquals(cause, flux.getError());
    assertEquals(1, sinkInvoked.get());
    verify(completionHandler).onComplete();
  }

  @Test
  public void testCheckedExceptionInSink() {
    final FluxException cause = new FluxException("Simulated");
    final AtomicInteger sinkInvoked = new AtomicInteger();
    final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);
    
    final Flux flux = new Flux()
        .onError(ExceptionHandler.nop())
        .cascade(Emitters.array(0, 1, 2).onComplete(completionHandler))
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
  public void testComplete_array() {
    final List<Integer> collected = new ArrayList<>();
    final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);
    
    final Flux flux = new Flux()
        .onError(ExceptionHandler.nop())
        .cascade(Emitters.array(0, 1, 2)
                 .withWorkerOptions(new WorkerOptions().withName("CustomName"))
                 .onComplete(completionHandler))
        .cascade(Sinks.collection(collected))
        .start();
    flux.joinSilently();
    
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    assertEquals(Arrays.asList(0, 1, 2), collected);
    verify(completionHandler).onComplete();
  }

  @Test
  public void testComplete_iterator() {
    final List<Integer> collected = new ArrayList<>();
    final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);
    
    final Flux flux = new Flux()
        .onError(ExceptionHandler.nop())
        .cascade(Emitters.iterator(IntStream.range(0, 3).iterator())
                 .withWorkerOptions(new WorkerOptions().withName("CustomName"))
                 .onComplete(completionHandler))
        .cascade(Sinks.collection(collected))
        .start();
    flux.joinSilently();
    
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    assertEquals(Arrays.asList(0, 1, 2), collected);
    verify(completionHandler).onComplete();
  }

  @Test
  public void testComplete_singleton() {
    final List<Integer> collected = new ArrayList<>();
    
    final Flux flux = new Flux()
        .onError(ExceptionHandler.nop())
        .cascade(Emitters.singleton(42))
        .cascade(Sinks.collection(collected))
        .start();
    flux.joinSilently();
    
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    assertEquals(Collections.singletonList(42), collected);
  }
  
  @Test
  public void testTerminateStage() {
    final List<Integer> collected = new ArrayList<>();
    final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);
    
    final Flux flux = new Flux()
        .onError(ExceptionHandler.nop())
        .cascade(Emitters
                 .<Integer>supplier(StageContext::terminate)
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
    final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);
    
    final Flux flux = new Flux()
        .onError(ExceptionHandler.nop())
        .cascade(Emitters.supplier(() -> "foo").onComplete(completionHandler))
        .cascade(Sinks.nop())
        .start();
    flux.terminate().joinSilently();
    
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    verify(completionHandler).onComplete();
  }
}
