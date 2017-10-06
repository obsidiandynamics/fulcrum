package com.obsidiandynamics.shell;

/**
 *  Thrown in methods that rely on the successful execution of a command.
 */
public final class CommandExecutionException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  CommandExecutionException(String m, Throwable cause) { super(m, cause); }
}