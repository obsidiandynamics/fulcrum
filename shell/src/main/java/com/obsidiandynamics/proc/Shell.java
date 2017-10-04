package com.obsidiandynamics.proc;

/**
 *  Definition of a shell - a wrapper for a command.
 */
public interface Shell {
  String[] toArgs(String... commandFrags);
  
  static ProcBuilder builder() {
    return new ProcBuilder();
  }
}
