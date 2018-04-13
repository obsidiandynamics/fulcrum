package com.obsidiandynamics.shell;

/**
 *  Executes a given command directly, without wrapping it
 *  in a shell.
 */
public final class NullShell implements Shell {
  private static final NullShell instance = new NullShell();
  
  public static NullShell getIntance() { return instance; }
  
  private NullShell() {}
  
  @Override
  public String[] prepare(String[] command) {
    return command;
  }

  @Override
  public CommandTransform getDefaultEcho() {
    return CommandTransform::splice;
  }
}
