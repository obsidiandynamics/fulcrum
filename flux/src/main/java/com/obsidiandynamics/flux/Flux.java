package com.obsidiandynamics.flux;

import static com.obsidiandynamics.func.Functions.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.worker.*;
import com.obsidiandynamics.worker.Terminator;

/**
 *  <b>Flux</b> is a pipeline that pushes events through a series of 
 *  <b>discrete stages</b>, assimilating a staged event-driven architectural (SEDA) 
 *  style and enabling the reactive programming (RP) paradigm. <p>
 *  
 *  A {@link DiscreteStage} in one of an {@link Emitter}, {@link Sink} or a 
 *  {@link Channel} (which is a combination of both an emitter and a sink). A pipeline must
 *  start with an emitter and must end with a sink, having a minimum of two stages.
 *  The intermediate stages are optional, each being a channel. There is no limit
 *  on the number of stages in a pipeline. <p>
 *  
 *  Flow through a {@link Flux} pipeline will commence when {@link Flux#start()} is
 *  invoked, and will continue until either one of the
 *  stages explicitly signals internal termination (by calling {@link StageContext#terminate()}),
 *  or the pipeline is terminated externally (via {@link Flux#terminate()}), or an
 *  exception is thrown from a stage. In the latter case, the thrown error will be
 *  presented to a registered {@link ExceptionHandler} instance, and will be made available
 *  via {@link Flux#getError()}. <p>
 *  
 *  The completion of any stage will trigger a cascade of events, starting from its
 *  immediate neighbours. The sink immediately downstream of a completed emitter 
 *  (or channel) will have
 *  its {@link Sink#onUpstreamComplete()} lifecycle method invoked. This should trigger
 *  any cleanup preparation within the downstream sink, but may not terminate the sink
 *  immediately — not until it has a chance to work through the backlog of upstream events
 *  (if any). The emitter immediately upstream of a completed sink (or channel) will have its
 *  {@link Emitter#onDownstreamComplete()} lifecycle method invoked. This should terminate
 *  the emitter immediately — no longer deemed necessary for the pipeline. The
 *  termination of the neighbouring stages will have a recursive effect, rippling through
 *  the remainder of the pipeline until all stages have completed. In other words,
 *  the completion of a stage has a draining effect on all downstream stages and a
 *  terminating effect on all upstream stages. <p>
 *  
 *  If the pipeline is terminated externally (via {@link Flux#terminate()}), each 
 *  stage will be forcibly terminated, 
 *  starting from the last stage and working upstream to the first. Stage lifecycle methods
 *  will still be invoked as stages are eventually marked as complete; however, the 
 *  entire extent of the pipeline will be terminated expediently — no draining effect is 
 *  to be expected.
 *  
 *  @see DiscreteStage
 */
public final class Flux implements Terminable, Joinable {
  private final class StageHolder implements StageController {
    final StageHolder previous;
    
    final DiscreteStage stage;
    
    StageHolder next;
    
    private final AtomicBoolean completing = new AtomicBoolean();
    
    private final AtomicBoolean signalled = new AtomicBoolean();
    
    private volatile boolean completed;
    
    StageHolder(StageHolder previous, DiscreteStage stage) {
      this.previous = previous;
      this.stage = stage;
      
      if (previous != null) {
        previous.setNext(this);
      }
    }
    
    void setNext(StageHolder next) {
      this.next = next;
    }
    
    void signalFromDownstream() {
      if (signalled.compareAndSet(false, true)) {
        synchronized (lifecycleLock) {
          ((Emitter<?>) stage).onDownstreamComplete();
        }
      }
    }
    
    void signalFromUpstream() {
      if (signalled.compareAndSet(false, true)) {
        synchronized (lifecycleLock) {
          ((Sink<?>) stage).onUpstreamComplete();
        }
      }
    }

    @Override
    public void complete(Throwable error) {
      if (completing.compareAndSet(false, true)) {
        if (previous != null) {
          previous.signalFromDownstream();
        }
        
        if (next != null) {
          next.signalFromUpstream();
        }
        
        ifPresentVoid(error, Flux.this::setError);
        completed = true;
        if (isComplete()) {
          completionHandlerHolder.fire(getError());
        }
      }
    }
  }
  
  private final Object lifecycleLock = new Object();
  
  private StageHolder tail;
  
  private boolean started;
  
  private Throwable error;
  
  private final PipelineCompletionHandlerHolder completionHandlerHolder = new PipelineCompletionHandlerHolder();
  
  private ExceptionHandler errorHandler = ExceptionHandler.nop();
  
  public final class IntermediateStage<E> {
    public <N> IntermediateStage<N> cascade(Channel<E, N> channel) {
      chain(channel);
      return new IntermediateStage<>();
    }
    
    public Flux cascade(Sink<E> sink) {
      chain(sink);
      return Flux.this;
    }
  }
  
  public <E> IntermediateStage<E> cascade(Emitter<E> emitter) {
    chain(emitter);
    return new IntermediateStage<>();
  }
  
  public Flux onError(ExceptionHandler errorHandler) {
    this.errorHandler = errorHandler;
    return this;
  }
  
  Flux chain(DiscreteStage stage) {
    mustExist(stage, "Stage cannot be null");
    tail = new StageHolder(tail, stage);
    return this;
  }
  
  public Flux onComplete(PipelineCompletionHandler completionHandler) {
    completionHandlerHolder.setHandler(completionHandler);
    return this;
  }
  
  public List<DiscreteStage> getStages() {
    final List<DiscreteStage> stages = new ArrayList<>();
    for (StageHolder holder = tail; holder != null; holder = holder.previous) {
      stages.add(holder.stage);
    }
    Collections.reverse(stages);
    return stages;
  }
  
  public Flux start() {
    mustExist(tail, illegalState("No stages in pipeline"));
    
    synchronized (lifecycleLock) {
      mustBeFalse(started, illegalState("Pipeline already started"));
      
      for (StageHolder holder = tail; holder != null; holder = holder.previous) {
        final DiscreteStage stage = holder.stage;
        if (stage instanceof Emitter) {
          mustExist(holder.next, illegalState("Missing stage after last " + Emitter.class.getSimpleName()));
          final DiscreteStage next = holder.next.stage;
          ensureStageType(next, Sink.class);
          ((Emitter<?>) stage).assignDownstream(Classes.cast(next));
        }
        
        if (stage instanceof Sink) {
          mustExist(holder.previous, illegalState("Missing stage before first " + Sink.class.getSimpleName()));
          final DiscreteStage previous = holder.previous.stage;
          ensureStageType(previous, Emitter.class);
        }
      }

      started = true;
      for (StageHolder holder = tail; holder != null; holder = holder.previous) {
        holder.stage.start(holder);
      }
    }
    
    return this;
  }
  
  private void ensureStageType(DiscreteStage stage, Class<? extends DiscreteStage> expectedType) {
    mustBeSubtype(stage, expectedType, 
                  illegalState("Incompatible stage: " + stage.getClass().getSimpleName() + 
                               ", expecting: " + expectedType.getSimpleName()));
  }

  @Override
  public boolean join(long timeoutMillis) throws InterruptedException {
    final Joiner joiner = Joiner.blank();
    for (StageHolder holder = tail; holder != null; holder = holder.previous) {
      joiner.add(holder.stage);
    }
    
    return joiner.join(timeoutMillis);
  }

  @Override
  public Joinable terminate() {
    final Terminator terminator = Terminator.blank();
    for (StageHolder holder = tail; holder != null; holder = holder.previous) {
      terminator.add(holder.stage);
    }
    
    terminator.terminate();
    return this;
  }
  
  public boolean isComplete() {
    if (tail == null) return false;
    
    for (StageHolder holder = tail; holder != null; holder = holder.previous) {
      if (! holder.completed) {
        return false;
      }
    }
    return true;
  }

  public boolean isError() {
    return error != null;
  }
  
  private void setError(Throwable error) {
    mustExist(error);
    errorHandler.onException("Exception in stage", error);
    this.error = error;
  }
  
  public Throwable getError() {
    return error;
  }
  
  /**
   *  Rethrows the captured error if one exists, otherwise exits peacefully.
   *  
   *  @throws Throwable If an error had previously occurred.
   */
  public void rethrowError() throws Throwable {
    if (error != null) throw error;
  }
}
