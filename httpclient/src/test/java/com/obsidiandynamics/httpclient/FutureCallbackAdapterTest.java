package com.obsidiandynamics.httpclient;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.*;
import org.mockito.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.httpclient.FutureCallbackAdapter.*;

public final class FutureCallbackAdapterTest {
  @Test
  public void testCompleted() {
    final FutureCallbackAdapter<Object> adapter = Classes.<FutureCallbackAdapter<Object>>cast(mock(FutureCallbackAdapter.class, Answers.CALLS_REAL_METHODS));
    adapter.completed("result");
    verify(adapter).onComplete(eq(CompletionType.NORMAL), eq("result"), isNull());
  }

  @Test
  public void testFailed() {
    final FutureCallbackAdapter<Object> adapter = Classes.<FutureCallbackAdapter<Object>>cast(mock(FutureCallbackAdapter.class, Answers.CALLS_REAL_METHODS));
    final Exception cause = new Exception("Simulated");
    adapter.failed(cause);
    verify(adapter).onComplete(eq(CompletionType.FAILED), isNull(), eq(cause));
  }

  @Test
  public void testCancelled() {
    final FutureCallbackAdapter<Object> adapter = Classes.<FutureCallbackAdapter<Object>>cast(mock(FutureCallbackAdapter.class, Answers.CALLS_REAL_METHODS));
    adapter.cancelled();
    verify(adapter).onComplete(eq(CompletionType.CANCELLED), isNull(), isNull());
  }
}
