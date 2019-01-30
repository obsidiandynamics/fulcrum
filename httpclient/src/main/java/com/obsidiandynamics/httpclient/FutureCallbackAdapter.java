package com.obsidiandynamics.httpclient;

import org.apache.http.concurrent.*;

/**
 *  An adaptation of {@link FutureCallback}, by default funnelling each of the three
 *  callback methods into a single handler method.
 *
 *  @param <T> The future result type returned by this callback.
 */
public interface FutureCallbackAdapter<T> extends FutureCallback<T> {
  enum CompletionType {
    NORMAL, FAILED, CANCELLED
  }
  
  /**
   *  Invoked when the callback completes.
   *  
   *  @param type The completion type.
   *  @param result The completion result (if completed with a non-{@code null} result).
   *  @param error The error (if completed with a failure).
   */
  void onComplete(CompletionType type, T result, Exception error);
  
  @Override
  default void completed(T result) {
    onComplete(CompletionType.NORMAL, result, null);
  }

  @Override
  default void failed(Exception ex) {
    onComplete(CompletionType.FAILED, null, ex);
  }

  @Override
  default void cancelled() {
    onComplete(CompletionType.CANCELLED, null, null);
  }
}
