package com.obsidiandynamics.flux;

import com.obsidiandynamics.worker.*;

/**
 *  A <b>discrete stage</b> is an element in a <b>Flux</b> pipeline. <p>
 *  
 *  A {@link DiscreteStage} can take on the traits of an {@link Emitter} or
 *  a {@link Sink}, depending on whether it publishes events or subscribes to them.
 *  It can also assume the characteristics of both, which makes it a 
 *  {@link Channel}. <p>
 *  
 *  A stage appearing before a peer stage is referred to as
 *  an <b>upstream</b> stage (in relation to its peer). Conversely, the peer is referred 
 *  to as being <b>downstream</b> of the former. Stages may be <b>active</b> 
 *  (independently scheduled) or <b>passive</b> (scheduled from an upstream stage). <p>
 *  
 *  A stage can be terminated at any point, either externally or from within the
 *  implementation of the stage. Upon successful termination, a stage is expected to
 *  invoke the {@link StageController#complete(Throwable)} method, signalling
 *  to the {@link Flux} pipeline that the stage in question has completed. In turn, the
 *  pipeline will cascade the completion event through the neighbouring stages, invoking
 *  their lifecycle methods (which has a recursive effect), and eventually
 *  completing all stages in the pipeline. <p>
 *  
 *  Clients are not expected to subclass {@link DiscreteStage} or any of its immediate
 *  derivatives {@link Emitter}, {@link Sink} or {@link Channel} directly. Instead, 
 *  routine implementations can be sourced from the {@link Emitters}, {@link Sinks}
 *  and {@link Channels} helper classes, which will collectively cover a large part 
 *  of the typical mapping, reduction, filtering, buffering and scheduling use cases.
 *  
 *  @see Flux
 */
public interface DiscreteStage extends Terminable, Joinable {
  /**
   *  Starts the stage. Stages are started from the tail-end of the pipeline, working
   *  upstream until all stages have started. <p>
   *  
   *  Active stages will begin emitting events as soon as they're started; passive
   *  stages (such as sinks and mappers/filters) will be driven from an upstream
   *  stage.
   *  
   *  @param controller An interface between the stage and its encompassing pipeline, allowing
   *                    the stage to signal completion.
   */
  void start(StageController controller);
}
