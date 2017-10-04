package com.obsidiandynamics.proc;

/**
 *  Executes a given command directly, without wrapping it
 *  in a shell.
 */
public final class Direct implements Shell {
  @Override
  public String[] toArgs(String... commandFrags) {
    return commandFrags;
  }
}
