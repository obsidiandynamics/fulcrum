package com.obsidiandynamics.flux;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

import org.assertj.core.api.*;
import org.junit.*;

public final class MappingChannelTest {
  @Test
  public void testComplete() {
    final List<String> collected = new ArrayList<>();
    final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);

    final Flux flux = new Flux()
        .cascade(Emitters.array(0, 1, 2))
        .cascade(Channels
                 .<Integer, String>map(String::valueOf)
                 .onComplete(completionHandler))
        .cascade(Sinks.collection(collected))
        .start();
    flux.joinSilently();

    assertTrue(flux.isComplete());
    assertFalse(flux.isError());
    assertEquals(Arrays.asList("0", "1", "2"), collected);
    verify(completionHandler).onComplete();
  }

  @Test
  public void testComplete_timestamped() {
    final List<Timestamped<Integer>> collected = new ArrayList<>();

    final Flux flux = new Flux()
        .cascade(Emitters.array(0, 1, 2))
        .cascade(Channels.timestamped())
        .cascade(Sinks.collection(collected))
        .start();
    flux.joinSilently();

    assertTrue(flux.isComplete());
    assertFalse(flux.isError());
    Assertions.assertThat(collected).extracting(Timestamped::getValue).containsExactly(0, 1, 2);
  }

  @Test
  public void testComplete_filter() {
    final List<Integer> collected = new ArrayList<>();

    final Flux flux = new Flux()
        .cascade(Emitters.array(0, 1, 2, 3, 4, 5))
        .cascade(Channels.filter(event -> event % 2 == 0))
        .cascade(Sinks.collection(collected))
        .start();
    flux.joinSilently();

    assertTrue(flux.isComplete());
    assertFalse(flux.isError());
    assertEquals(Arrays.asList(0, 2, 4), collected);
  }

  @Test
  public void testComplete_distinctWithIdentityExtractor() {
    final List<Integer> collected = new ArrayList<>();

    final Flux flux = new Flux()
        .cascade(Emitters.array(0, 1, 2, 1, 3, 2, 1))
        .cascade(Channels.distinct())
        .cascade(Sinks.collection(collected))
        .start();
    flux.joinSilently();

    assertTrue(flux.isComplete());
    assertFalse(flux.isError());
    assertEquals(Arrays.asList(0, 1, 2, 3), collected);
  }

  @Test
  public void testComplete_distinctWithCustomExtractor() {
    final List<Timestamped<Integer>> collected = new ArrayList<>();

    final Flux flux = new Flux()
        .cascade(Emitters.array(0, 1, 2, 1, 3, 2, 1))
        .cascade(Channels.timestamped())
        .cascade(Channels.distinct(Timestamped::getValue))
        .cascade(Sinks.collection(collected))
        .start();
    flux.joinSilently();

    assertTrue(flux.isComplete());
    assertFalse(flux.isError());
    Assertions.assertThat(collected).extracting(Timestamped::getValue).containsExactly(0, 1, 2, 3);
  }

  @Test
  public void testComplete_skipAndTake() {
    final List<Integer> collected = new ArrayList<>();

    final Flux flux = new Flux()
        .cascade(Emitters.stream(IntStream.range(0, 10).boxed()))
        .cascade(Channels.skip(3))
        .cascade(Channels.take(5))
        .cascade(Sinks.collection(collected))
        .start();
    flux.joinSilently();

    assertTrue(flux.isComplete());
    assertFalse(flux.isError());
    assertEquals(Arrays.asList(3, 4, 5, 6, 7), collected);
  }

  @Test
  public void testComplete_reduction() {
    final List<Integer> collected = new ArrayList<>();
    final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);

    final AtomicInteger sum = new AtomicInteger();
    final Flux flux = new Flux()
        .cascade(Emitters.array(0, 1, 2, 3, 4, 5, 6))
        .cascade(Channels
                 .<Integer, Integer>map((context, event) -> {
                   sum.addAndGet(event);
                   if (event % 2 == 0) {
                     try {
                       context.emit(sum.get());
                     } finally {
                       sum.set(0);
                     }
                   }
                 })
                 .onComplete(completionHandler))
        .cascade(Sinks.collection(collected))
        .start();
    flux.joinSilently();

    assertTrue(flux.isComplete());
    assertFalse(flux.isError());
    assertEquals(Arrays.asList(0, 3, 7, 11), collected);
    verify(completionHandler).onComplete();
  }

  @Test
  public void testComplete_flatMap() {
    final List<Integer> collected = new ArrayList<>();
    final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);

    final Flux flux = new Flux()
        .cascade(Emitters.array(0, 10, 20))
        .cascade(Channels
                 .<Integer, Integer>flatMap(event -> Arrays.asList(event, event + 1).iterator())
                 .onComplete(completionHandler))
        .cascade(Sinks.collection(collected))
        .start();
    flux.joinSilently();

    assertTrue(flux.isComplete());
    assertFalse(flux.isError());
    assertEquals(Arrays.asList(0, 1, 10, 11, 20, 21), collected);
    verify(completionHandler).onComplete();
  }

  @Test
  public void testTerminateStage() {
    final List<String> collected = new ArrayList<>();
    final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);

    final Flux flux = new Flux()
        .cascade(Emitters.array(0, 1, 2))
        .cascade(Channels.<Integer, String>map((context, event) -> {
          context.terminate();
        }).onComplete(completionHandler))
        .cascade(Sinks.collection(collected))
        .start();
    flux.joinSilently();
    assertTrue(flux.isComplete());
    assertFalse(flux.isError());
    assertEquals(Collections.emptyList(), collected);
    verify(completionHandler).onComplete();
  }

  @Test
  public void testTerminateStage_guardFromNewElements() {
    final List<Integer> source = Arrays.asList(0, 1, 2);
    final AtomicInteger invoked = new AtomicInteger();

    final Flux flux = new Flux()
        .cascade(new EagerEmitter<>(source))
        .cascade(Channels.map((context, event) -> {
          context.terminate();
          invoked.incrementAndGet();
        }))
        .cascade(Sinks.nop())
        .start();
    flux.joinSilently();
    assertTrue(flux.isComplete());
    assertFalse(flux.isError());
    assertEquals(1, invoked.get());
  }
}
