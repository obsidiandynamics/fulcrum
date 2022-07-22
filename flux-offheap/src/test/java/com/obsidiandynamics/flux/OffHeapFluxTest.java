package com.obsidiandynamics.flux;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.util.*;
import com.obsidiandynamics.func.*;
import com.obsidiandynamics.junit.*;

@RunWith(Parameterized.class)
public final class OffHeapFluxTest {
  @Parameterized.Parameters
  public static List<Object[]> data() {
    return TestCycle.timesQuietly(1);
  }
  
  private static Pool<Kryo> newKryoPool() {
    return new Pool<Kryo>(true, false) {
      @Override
      protected Kryo create() {
        final Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.setRegistrationRequired(false);
        kryo.register(Timestamped.class, new KryoTimestampedSerializer());
        return kryo;
      }
    };
  }
  
  private static OffHeapBackingQueueFactory newOffHeapBackingQueueFactory() {
    return new OffHeapBackingQueueFactory(newKryoPool());
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
        .cascade(Channels.buffered(2, newOffHeapBackingQueueFactory()))
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
  public void test4Stage_complete() {
    final List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
    final List<Timestamped<String>> collected = new ArrayList<>();
    final PipelineCompletionHandler completionHandler = mock(PipelineCompletionHandler.class);
    final ExceptionHandler errorHandler = mock(ExceptionHandler.class);
    
    final Flux flux = new Flux()
        .onError(errorHandler)
        .onComplete(completionHandler)
        .cascade(Emitters.stream(ints.stream().map(Timestamped::new)))
        .cascade(Channels.buffered(2, newOffHeapBackingQueueFactory()))
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
        .cascade(Channels.buffered(2, newOffHeapBackingQueueFactory()))
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
        .cascade(Channels.buffered(2, newOffHeapBackingQueueFactory()))
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
        .cascade(Channels.buffered(2, newOffHeapBackingQueueFactory()))
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
        .cascade(Channels.buffered(2, newOffHeapBackingQueueFactory()))
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
        .cascade(Channels.buffered(2, newOffHeapBackingQueueFactory()))
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
}
