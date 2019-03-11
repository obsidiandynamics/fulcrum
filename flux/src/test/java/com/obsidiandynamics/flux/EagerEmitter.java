package com.obsidiandynamics.flux;

import com.obsidiandynamics.worker.*;

/**
 *  A type of {@link Emitter} that ignores downstream events, and will
 *  continue to publish events uninterrupted until it exhausts its {@code source}.
 *
 *  @param <E> Event type.
 */
final class EagerEmitter<E> implements Emitter<E> {
  private final Iterable<E> source;
  
  private Sink<E> downstream;
  
  private Thread thread;
  
  public EagerEmitter(Iterable<E> source) {
    this.source = source;
  }

  @Override
  public void start(StageController controller) {
    thread = new Thread(() -> {
      try {
        for (E next : source) {
          downstream.onNext(next);
        }
        controller.complete(null);
      } catch (InterruptedException e) {
        controller.complete(null);
      } catch (Throwable e) {
        controller.complete(FluxSupport.suppressInterruptedException(e));
      }
    }, EagerEmitter.class.getSimpleName() + "-driver");
    thread.setDaemon(true);
    thread.start();
  }

  @Override
  public Joinable terminate() {
    return this;
  }

  @Override
  public boolean join(long timeoutMillis) throws InterruptedException {
    if (thread != null) {
      thread.join(timeoutMillis);
      return ! thread.isAlive();
    } else {
      return true;
    }
  }

  @Override
  public void assignDownstream(Sink<E> downstream) {
    this.downstream = downstream;
  }

  @Override
  public void onDownstreamComplete() {
    // don't care, let the thread rip until finished
  }
}
