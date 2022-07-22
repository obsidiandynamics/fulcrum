package com.obsidiandynamics.flux;

import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.assertj.core.api.*;
import org.junit.*;

import com.obsidiandynamics.await.*;
import com.obsidiandynamics.func.*;
import com.obsidiandynamics.threads.*;
import com.obsidiandynamics.worker.*;

public abstract class AbstractBufferedChannelTest {
  protected abstract BackingQueueFactory getBackingQueueFactory();
  
  @Test
  public void testUncheckedExceptionInSink() {
    final RuntimeException cause = new RuntimeException("Simulated");
    final AtomicInteger sinkInvoked = new AtomicInteger();
    final Flux flux = new Flux()
        .onError(ExceptionHandler.nop())
        .cascade(Emitters.array(0, 1, 2))
        .cascade(Channels.buffered(1, getBackingQueueFactory()))
        .cascade(Sinks.consumer(__ -> {
          sinkInvoked.incrementAndGet();
          throw cause;
        }))
        .start();
    flux.joinSilently();
    assertTrue(flux.isComplete());
    assertTrue(flux.isError());
    Assertions.assertThat(flux.getError()).isEqualTo(cause);
    assertEquals(1, sinkInvoked.get());
  }

  @Test
  public void testCheckedExceptionInSink() {
    final FluxException cause = new FluxException("Simulated");
    final AtomicInteger sinkInvoked = new AtomicInteger();
    final Flux flux = new Flux()
        .onError(ExceptionHandler.nop())
        .cascade(Emitters.array(0, 1, 2))
        .cascade(Channels.buffered(1, getBackingQueueFactory()))
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
  }

  @Test
  public void testTerminate_starvedEmitter() {
    final AtomicInteger collected = new AtomicInteger();
    final Flux flux = new Flux()
        .cascade(Emitters.array(0, 1, 2))
        .cascade(Channels.sleep(1))
        .cascade(Channels.<Integer>buffered(1, getBackingQueueFactory()).withQueuePollInterval(0))
        .cascade(Sinks.consumer(collected::set))
        .start();

    Timesert.wait(60_000).untilTrue(() -> collected.get() == 2);
    flux.terminate().joinSilently();
    
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
  }

  @Test
  public void testTerminate_emitterWithFreeQueue() {
    final List<Integer> collected = new ArrayList<>();
    
    final Flux flux = new Flux()
        .cascade(Emitters.<Integer>supplier(StageContext::terminate))
        .cascade(Channels.buffered(1, getBackingQueueFactory()))
        .cascade(Sinks.consumer((__context, event) -> {
          collected.add(event);
        }))
        .start();

    flux.joinSilently();
    
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    assertEquals(Collections.emptyList(), collected);
  }

  @Test
  public void testTerminate_emitterWithBackloggedQueue() {
    final List<Integer> collected = new ArrayList<>();
    final AtomicInteger invocations = new AtomicInteger();
    final CyclicBarrier terminationBarrier = new CyclicBarrier(2);
    
    final Flux flux = new Flux()
        .cascade(Emitters.<Integer>supplier(context -> {
          final int next = invocations.getAndIncrement();
          if (next < 2) {
            context.emit(next);
          } else {
            context.terminate();
          }
        }).onComplete(() -> { Threads.await(terminationBarrier); }))
        .cascade(Channels.buffered(1, getBackingQueueFactory()))
        .cascade(Sinks.consumer((__context, event) -> {
          collected.add(event);
          if (event == 0) {
            Threads.await(terminationBarrier);
          }
        }))
        .start();

    flux.joinSilently();
    
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    assertEquals(Arrays.asList(0, 1), collected);
  }

  @Test
  public void testTerminate_emitterWithIndefinitelyBackloggedQueue() {
    final AtomicInteger invocations = new AtomicInteger();
    final CyclicBarrier terminationBarrier = new CyclicBarrier(2);
    
    final Flux flux = new Flux()
        .cascade(Emitters.<Integer>supplier(context -> {
          final int next = invocations.getAndIncrement();
          if (next < 2) {
            context.emit(next);
          } else {
            context.terminate();
          }
        }).onComplete(() -> { Threads.await(terminationBarrier); }))
        .cascade(Channels.buffered(1, getBackingQueueFactory()))
        .cascade(Sinks.consumer((__context, event) -> {
          Threads.sleep(Long.MAX_VALUE);
        }))
        .start();

    Threads.await(terminationBarrier);
    final BufferedChannel<?> bufferedChannel = (BufferedChannel<?>) flux.getStages().get(1);
    bufferedChannel.terminate();
    flux.joinSilently();
    
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
  }

  @Test
  public void testComplete() {
    final List<Integer> collected = new ArrayList<>();
    
    final Flux flux = new Flux()
        .cascade(Emitters.array(0, 1, 2))
        .cascade(Channels.<Integer>buffered(2, getBackingQueueFactory()).withWorkerOptions(new WorkerOptions().withName("CustomName")))
        .cascade(Sinks.collection(collected))
        .start();
    flux.joinSilently();
    
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    assertEquals(Arrays.asList(0, 1, 2), collected);
  }
  
  @Test
  public void testTerminatePipeline() {
    final Flux flux = new Flux()
        .onError(ExceptionHandler.forPrintStream(System.err))
        .cascade(Emitters.supplier(() -> "foo"))
        .cascade(Channels.buffered(getBackingQueueFactory()))
        .cascade(Sinks.nop())
        .start();
    flux.terminate().joinSilently();
    
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
  }
}
