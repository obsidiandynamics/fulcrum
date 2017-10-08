package com.obsidiandynamics.shell;

/**
 *  Definition of a shell - a wrapper for a command.
 */
public interface Shell {
  /**
   *  Prepares a command for execution, by transforming the given {@code command} array 
   *  into one that is effectively wrapped by this shell.
   *  
   *  @param command The raw command.
   *  @return The prepared command.
   */
  String[] prepare(String[] command);
  
  /**
   *  Obtains the default transform that should by applied when echoing the command for
   *  this shell.
   *  
   *  @return The default {@link CommandTransform}.
   */
  CommandTransform getDefaultEcho();
  
  /**
   *  Creates a new builder.
   *  
   *  @return A new {@link ShellBuilder} instance.
   */
  static ShellBuilder builder() {
    return new ShellBuilder();
  }
  
  /**
   *  Obtains a new instance of the default shell.
   *  
   *  @return The default shell.
   */
  static Shell getDefault() {
    return new BourneShell();
  }
}
