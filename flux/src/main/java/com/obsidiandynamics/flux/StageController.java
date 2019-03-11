package com.obsidiandynamics.flux;

/**
 *  A pinhole interface between a {@link DiscreteStage} and its encompassing {@link Flux} pipeline, 
 *  allowing the stage to communicate with the pipeline, without necessarily exposing the entire
 *  pipeline API.
 */
public interface StageController {
  /**
   *  Signals to the pipeline that the stage that owns this {@link StageController} instance
   *  has completed (successfully or in error).
   *  
   *  @param error The error, if one was observed.
   */
  void complete(Throwable error);
}
