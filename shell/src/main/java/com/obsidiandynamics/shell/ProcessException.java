package com.obsidiandynamics.shell;

/**
 *  Thrown if a command couldn't be run by the {@link ShellBuilder}.
 */
public final class ProcessException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  ProcessException(String m, Throwable cause) {
    super(m, cause);
  }
}
