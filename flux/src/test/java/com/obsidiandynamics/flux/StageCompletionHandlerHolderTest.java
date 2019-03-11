package com.obsidiandynamics.flux;

import static org.mockito.Mockito.*;

import org.junit.*;

public final class StageCompletionHandlerHolderTest {
  @Test
  public void testFire_twice() {
    final StageCompletionHandler handler = mock(StageCompletionHandler.class);
    final StageCompletionHandlerHolder holder = new StageCompletionHandlerHolder();
    holder.setHandler(handler);
    
    holder.fire();
    verify(handler).onComplete();
    
    holder.fire();
    verify(handler).onComplete();
  }
}
