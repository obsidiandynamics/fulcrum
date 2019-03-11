package com.obsidiandynamics.flux;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.*;

public final class ConsumerSinkTest {
  @Test
  public void testComplete() {
    final List<Integer> collected = new ArrayList<>();
    final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);
    
    final Flux flux = new Flux()
        .cascade(Emitters.array(0, 1, 2))
        .cascade(Sinks.collection(collected).onComplete(completionHandler))
        .start();
    flux.joinSilently();
    
    assertTrue(flux.isComplete());
    assertFalse(flux.isError());
    assertEquals(Arrays.asList(0, 1, 2), collected);
    verify(completionHandler).onComplete();
  }
  
  @Test
    public void testTerminate() {
      final List<Integer> collected = new ArrayList<>();
      final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);
      
      final Flux flux = new Flux()
          .cascade(Emitters.array(0, 1, 2))
          .cascade(Sinks
                   .<Integer>consumer((context, event) -> {
                     context.terminate();
                     collected.add(event);
                   })
                   .onComplete(completionHandler))
          .start();
      flux.joinSilently();
      assertTrue(flux.isComplete());
      assertFalse(flux.isError());
      assertEquals(Collections.singletonList(0), collected);
      verify(completionHandler).onComplete();
    }
  
  @Test
    public void testTerminate_guardFromNewElements() {
      final List<Integer> source = Arrays.asList(0, 1, 2);
      final List<Integer> collected = new ArrayList<>();
      final StageCompletionHandler completionHandler = mock(StageCompletionHandler.class);
      
      final Flux flux = new Flux()
          .cascade(new EagerEmitter<>(source))
          .cascade(Sinks
                   .<Integer>consumer((context, event) -> {
                     context.terminate();
                     collected.add(event);
                   })
                   .onComplete(completionHandler))
          .start();
      flux.joinSilently();
      assertTrue(flux.isComplete());
      assertFalse(flux.isError());
      assertEquals(Collections.singletonList(0), collected);
      verify(completionHandler).onComplete();
    }
}
