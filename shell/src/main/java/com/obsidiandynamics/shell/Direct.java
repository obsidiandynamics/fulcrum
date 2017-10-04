package com.obsidiandynamics.shell;

/**
 *  Executes a given command directly, without wrapping it
 *  in a shell.
 */
public final class Direct implements Shell {
  @Override
  public String[] prepare(String... command) {
    return command;
  }
}
