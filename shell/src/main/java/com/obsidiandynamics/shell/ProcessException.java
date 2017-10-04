package com.obsidiandynamics.shell;

public final class ProcessException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  ProcessException(String m, Throwable cause) {
    super(m, cause);
  }
}
