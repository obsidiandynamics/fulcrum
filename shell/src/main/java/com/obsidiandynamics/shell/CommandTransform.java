package com.obsidiandynamics.shell;

import java.util.function.*;

import com.obsidiandynamics.concat.*;

/**
 *  Transforms a command array into a single string.
 */
@FunctionalInterface
public interface CommandTransform extends Function<String[], String> {
  /**
   *  Splices the command arguments using a single whitespace character as
   *  the delimiter.
   *  
   *  @param command The command.
   *  @return The spliced {@code command}.
   */
  static String splice(String[] command) {
    return new Concat().appendArray(" ", (Object[]) command).toString();
  }
}
