package com.obsidiandynamics.shell;

import java.io.*;

/**
 *  An entity capable of running a {@link Process}.
 */
public interface ProcessExecutor {
  /**
   *  Runs the given {@code command} array, resulting in a {@code Process}.
   *  
   *  @param command The command to run.
   *  @return The resulting {@link Process} instance.
   *  @throws IOException If an I/O error occurs.
   */
  Process run(String[] command) throws IOException;
  
  /**
   *  Attempts to run the given {@code command} array, returning a {@link Process}
   *  if successful, or {@code null} if an error occurred behind the scenes.
   *  
   *  @param command The command to run.
   *  @return The resulting {@link Process} instance, or {@code null} if an error occured.
   */
  default Process tryRun(String[] command) {
    try {
      return run(command);
    } catch (IOException e) {
      return null;
    }
  }
  
  /**
   *  Runs the given {@code command}, returning true if successful.
   *  
   *  @param command The command to run.
   *  @return True if the run succeeded.
   */
  default boolean canTryRun(String[] command) {
    return tryRun(command) != null;
  }
  
  /**
   *  Obtains a new instance of the default process executor.
   *  
   *  @return The default executor.
   */
  static ProcessExecutor getDefault() {
    return new DefaultProcessExecutor();
  }
}
