package com.obsidiandynamics.flux;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

import org.assertj.core.api.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.junit.*;

@RunWith(Parameterized.class)
public final class FluxTest {
  @Parameterized.Parameters
  public static List<Object[]> data() {
    return TestCycle.timesQuietly(1);
  }
  
  @Test
  public void testGetStages() {
    final SupplierEmitter<Integer> emitter = Emitters.array(0, 1, 2);
    final BufferedChannel<Integer> channel = Channels.buffered();
    final ConsumerSink<Integer> sink = Sinks.nop();
    
    final Flux flux = new Flux()
        .cascade(emitter)
        .cascade(channel)
        .cascade(sink);
    
    Assertions.assertThat(flux.getStages()).containsExactly(emitter, channel, sink);
  }
  
  @Test
  public void testStart_emptyPipeline() throws Throwable {
    final PipelineCompletionHandler completionHandler = mock(PipelineCompletionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    
    final Flux flux = new Flux()
        .onError(errorHandler)
        .onComplete(completionHandler);
    Assertions.assertThatThrownBy(flux::start)
    .isInstanceOf(IllegalStateException.class).hasMessage("No stages in pipeline");
    assertFalse(flux.isComplete());
    assertNull(flux.getError());
    verify(completionHandler, never()).onComplete(any());
    verify(errorHandler, never()).onException(any(), any());
    flux.rethrowError();
  }
  
  @Test
  public void testStart_startTwice() {
    final PipelineCompletionHandler completionHandler = mock(PipelineCompletionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    
    final Flux flux = new Flux()
        .onError(errorHandler)
        .onComplete(completionHandler)
        .cascade(Emitters.array(0, 1, 2))
        .cascade(Sinks.nop()).start();
    
    Assertions.assertThatThrownBy(() -> {
      try {
        flux.start();
      } finally {
        flux.joinSilently();
      }
    }).isInstanceOf(IllegalStateException.class).hasMessage("Pipeline already started");
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    verify(completionHandler).onComplete(isNull());
    verify(errorHandler, never()).onException(any(), any());
  }
  
  @Test
  public void testStart_missingStageBeforeFirst() {
    final PipelineCompletionHandler completionHandler = mock(PipelineCompletionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    final Flux flux = new Flux()
        .onError(errorHandler)
        .onComplete(completionHandler)
        .chain(Sinks.nop());
    
    Assertions.assertThatThrownBy(flux::start)
    .isInstanceOf(IllegalStateException.class).hasMessage("Missing stage before first Sink");
    assertFalse(flux.isComplete());
    assertNull(flux.getError());
    verify(completionHandler, never()).onComplete(any());
    verify(errorHandler, never()).onException(any(), any());
  }

  @Test
  public void testStart_incompatibleStage() {
    final PipelineCompletionHandler completionHandler = mock(PipelineCompletionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    final Flux flux = new Flux()
        .onError(errorHandler)
        .onComplete(completionHandler)
        .chain(Emitters.array())
        .chain(Sinks.nop())
        .chain(Sinks.nop());
    
    Assertions.assertThatThrownBy(flux::start)
    .isInstanceOf(IllegalStateException.class).hasMessage("Incompatible stage: ConsumerSink, expecting: Emitter");
    assertFalse(flux.isComplete());
    assertNull(flux.getError());
    verify(completionHandler, never()).onComplete(any());
    verify(errorHandler, never()).onException(any(), any());
  }
  
  @Test
  public void testStart_missingStageAfterLast() {
    final PipelineCompletionHandler completionHandler = mock(PipelineCompletionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    final Flux flux = new Flux()
        .onError(errorHandler)
        .onComplete(completionHandler)
        .chain(Emitters.array());
    
    Assertions.assertThatThrownBy(flux::start)
    .isInstanceOf(IllegalStateException.class).hasMessage("Missing stage after last Emitter");
    assertFalse(flux.isComplete());
    assertNull(flux.getError());
    verify(completionHandler, never()).onComplete(any());
    verify(errorHandler, never()).onException(any(), any());
  }
  
  @Test
  public void testCascade_nullStage() {
    Assertions.assertThatThrownBy(() -> {
      new Flux().cascade(null);
    }).isInstanceOf(NullArgumentException.class).hasMessage("Stage cannot be null");
  }
  
  @Test
  public void test2Stage_complete() {
    final List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
    final List<Integer> collected = new ArrayList<>();
    final PipelineCompletionHandler completionHandler = mock(PipelineCompletionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    
    final Flux flux = new Flux()
        .onError(errorHandler)
        .onComplete(completionHandler)
        .cascade(Emitters.stream(ints.stream()))
        .cascade(Sinks.collection(collected))
        .start();
    
    flux.joinSilently();
    
    assertEquals(ints, new ArrayList<>(collected));
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    verify(completionHandler).onComplete(isNull());
    verify(errorHandler, never()).onException(any(), any());
  }
  
  @Test
  public void test3Stage_complete() {
    final List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
    final List<Integer> collected = new ArrayList<>();
    final PipelineCompletionHandler completionHandler = mock(PipelineCompletionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    
    final Flux flux = new Flux()
        .onError(errorHandler)
        .onComplete(completionHandler)
        .cascade(Emitters.stream(ints.stream()))
        .cascade(Channels.buffered(2))
        .cascade(Sinks.collection(collected))
        .start();
    
    flux.joinSilently();
    
    assertEquals(ints, new ArrayList<>(collected));
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    verify(completionHandler).onComplete(isNull());
    verify(errorHandler, never()).onException(any(), any());
  }
  
  @Test
  public void test3Stage_terminate() {
    final List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
    final List<Integer> collected = new ArrayList<>();
    final PipelineCompletionHandler completionHandler = mock(PipelineCompletionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    
    final Flux flux = new Flux()
        .onError(errorHandler)
        .onComplete(completionHandler)
        .cascade(Emitters.iterable(ints))
        .cascade(Channels.sleep(10_000))
        .cascade(Sinks.collection(collected))
        .start();
    
    assertFalse(flux.isComplete());
    flux.terminate().joinSilently();
    
    assertTrue(collected.size() < ints.size());
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    verify(completionHandler).onComplete(isNull());
    verify(errorHandler, never()).onException(any(), any());
  }
  
  @Test
  public void test4Stage_complete() {
    final List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
    final List<Timestamped<String>> collected = new ArrayList<>();
    final PipelineCompletionHandler completionHandler = mock(PipelineCompletionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    
    final Flux flux = new Flux()
        .onError(errorHandler)
        .onComplete(completionHandler)
        .cascade(Emitters.stream(ints.stream().map(Timestamped::new)))
        .cascade(Channels.buffered(2))
        .cascade(Channels.map(Timestamped.mapPreserve(String::valueOf)))
        .cascade(Sinks.collection(collected))
        .start();
    
    flux.joinSilently();

    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    assertEquals(ints.stream().map(String::valueOf).collect(Collectors.toList()), 
                 collected.stream().map(Timestamped::getValue).collect(Collectors.toList()));
    verify(completionHandler).onComplete(isNull());
    verify(errorHandler, never()).onException(any(), any());
  }
  
  @Test
  public void testTerminateStage_emitterWithSlowEmission() {
    final List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
    final List<Integer> collected = new ArrayList<>();
    final PipelineCompletionHandler completionHandler = mock(PipelineCompletionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    
    final Flux flux = new Flux()
        .onError(errorHandler)
        .onComplete(completionHandler)
        .cascade(Emitters.iterable(ints))
        .cascade(Channels.sleep(0, 10))
        .cascade(Channels.buffered(2))
        .cascade(Sinks.collection(collected))
        .start();
    
    flux.joinSilently();
    
    assertEquals(ints, new ArrayList<>(collected));
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    verify(completionHandler).onComplete(isNull());
    verify(errorHandler, never()).onException(any(), any());
  }
  
  @Test
  public void testTerminateStage_emitterWithSlowSink() {
    final List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
    final List<Integer> collected = new ArrayList<>();
    final PipelineCompletionHandler completionHandler = mock(PipelineCompletionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    
    final Flux flux = new Flux()
        .onError(errorHandler)
        .onComplete(completionHandler)
        .cascade(Emitters.iterable(ints))
        .cascade(Channels.buffered(2))
        .cascade(Channels.sleep(0, 10))
        .cascade(Sinks.collection(collected))
        .start();
    
    flux.joinSilently();
    
    assertEquals(ints, new ArrayList<>(collected));
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    verify(completionHandler).onComplete(isNull());
    verify(errorHandler, never()).onException(any(), any());
  }
  
  @Test
  public void testTerminateStage_sink() {
    final List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
    final List<Integer> collected = new ArrayList<>();
    final PipelineCompletionHandler completionHandler = mock(PipelineCompletionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    
    final Flux flux = new Flux()
        .onError(errorHandler)
        .onComplete(completionHandler)
        .cascade(Emitters.iterable(ints))
        .cascade(Sinks.consumer((context, event) -> {
          context.terminate();
          collected.add(event);
        }))
        .start();
    
    flux.joinSilently();
    
    assertEquals(Collections.singletonList(0), new ArrayList<>(collected));
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    verify(completionHandler).onComplete(isNull());
    verify(errorHandler, never()).onException(any(), any());
  }
  
  @Test
  public void testTerminateStage_sinkWithSlowEmitter() {
    final List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
    final List<Integer> collected = new ArrayList<>();
    final PipelineCompletionHandler completionHandler = mock(PipelineCompletionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    
    final Flux flux = new Flux()
        .onError(errorHandler)
        .onComplete(completionHandler)
        .cascade(Emitters.iterable(ints))
        .cascade(Channels.sleep(0, 10))
        .cascade(Channels.buffered(2))
        .cascade(Sinks.consumer((context, event) -> {
          context.terminate();
          collected.add(event);
        }))
        .start();
    
    flux.joinSilently();
    
    assertEquals(Collections.singletonList(0), new ArrayList<>(collected));
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    verify(completionHandler).onComplete(isNull());
    verify(errorHandler, never()).onException(any(), any());
  }
  
  @Test
  public void testTerminateStage_mapperWithSlowSink() {
    final List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
    final List<Integer> collected = new ArrayList<>();
    final PipelineCompletionHandler completionHandler = mock(PipelineCompletionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    
    final AtomicInteger allow = new AtomicInteger(1);
    final Flux flux = new Flux()
        .onError(errorHandler)
        .onComplete(completionHandler)
        .cascade(Emitters.iterable(ints))
        .cascade(Channels.<Integer, Integer>map((context, input) -> {
          if (allow.getAndDecrement() > 0) {
            context.emit(input);
          } else {
            context.terminate();
          }
        }))
        .cascade(Channels.buffered(2))
        .cascade(Channels.sleep(10))
        .cascade(Sinks.collection(collected))
        .start();
    
    flux.joinSilently();
    
    assertEquals(Collections.singletonList(0), new ArrayList<>(collected));
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    verify(completionHandler).onComplete(isNull());
    verify(errorHandler, never()).onException(any(), any());
  }
  
  @Test
  public void testTerminateStage_mapperWithVerySlowSink() {
    final List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
    final List<Integer> collected = new ArrayList<>();
    final PipelineCompletionHandler completionHandler = mock(PipelineCompletionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    
    final Flux flux = new Flux()
        .onError(errorHandler)
        .onComplete(completionHandler)
        .cascade(Emitters.iterable(ints))
        .cascade(Channels.<Integer, Integer>map((context, __input) -> {
          context.terminate();
        }))
        .cascade(Channels.buffered(2))
        .cascade(Channels.sleep(Long.MAX_VALUE))
        .cascade(Sinks.collection(collected))
        .start();
    
    flux.joinSilently();
    
    assertEquals(Collections.emptyList(), new ArrayList<>(collected));
    assertTrue(flux.isComplete());
    assertNull(flux.getError());
    verify(completionHandler).onComplete(isNull());
    verify(errorHandler, never()).onException(any(), any());
  }
  
  @Test
  public void testUncheckedException() {
    final RuntimeException cause = new RuntimeException("Simulated");
    final PipelineCompletionHandler completionHandler = mock(PipelineCompletionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    
    final Flux flux = new Flux()
        .onError(errorHandler)
        .onComplete(completionHandler)
        .cascade(Emitters.array(0, 1, 2))
        .cascade(Sinks.consumer(__ -> {
          throw cause;
        }))
        .start();
    flux.joinSilently();
    assertTrue(flux.isComplete());
    assertTrue(flux.isError());
    assertEquals(cause, flux.getError());
    verify(completionHandler).onComplete(eq(flux.getError()));
    verify(errorHandler).onException(eq("Exception in stage"), eq(flux.getError()));
    Assertions.assertThatThrownBy(flux::rethrowError).isEqualTo(cause);
  }
}
