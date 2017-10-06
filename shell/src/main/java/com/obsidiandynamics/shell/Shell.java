package com.obsidiandynamics.shell;

/**
 *  Definition of a shell - a wrapper for a command.
 */
public interface Shell {
  String[] prepare(String... command);
  
  static ShellBuilder builder() {
    return new ShellBuilder();
  }
}
