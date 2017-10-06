package com.obsidiandynamics.shell;

import java.io.*;

/**
 *  An entity capable of running a {@link Process}.
 */
public interface ProcessExecutor {
  Process run(String... command) throws IOException;
  
  default Process tryRun(String... command) {
    try {
      return run(command);
    } catch (IOException e) {
      return null;
    }
  }
  
  default boolean canTryRun(String... command) {
    return tryRun(command) != null;
  }
}
