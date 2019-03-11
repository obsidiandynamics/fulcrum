package com.obsidiandynamics.flux;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.*;

public final class PipelineCompletionHandlerHolderTest {
  @Test
  public void testFire_twice() {
    final PipelineCompletionHandler handler = mock(PipelineCompletionHandler.class);
    final PipelineCompletionHandlerHolder holder = new PipelineCompletionHandlerHolder();
    final FluxException error = new FluxException("Simulated");
    holder.setHandler(handler);
    
    holder.fire(error);
    verify(handler).onComplete(eq(error));
    
    holder.fire(null);
    verify(handler).onComplete(eq(error));
  }
}
