package com.obsidiandynamics.shell;

/**
 *  Definition of a shell - a wrapper for a command.
 */
public interface Shell {
  String[] prepare(String... command);
  
  static ProcBuilder builder() {
    return new ProcBuilder();
  }
}
