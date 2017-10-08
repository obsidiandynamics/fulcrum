package com.obsidiandynamics.shell;

/**
 *  Executes a given command directly, without wrapping it
 *  in a shell.
 */
public final class NullShell implements Shell {
  @Override
  public String[] prepare(String[] command) {
    return command;
  }

  @Override
  public CommandTransform getDefaultEcho() {
    return CommandTransform::splice;
  }
}
