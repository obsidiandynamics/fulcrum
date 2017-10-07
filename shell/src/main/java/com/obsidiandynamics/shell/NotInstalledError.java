package com.obsidiandynamics.shell;

public final class NotInstalledError extends AssertionError {
  private static final long serialVersionUID = 1L;
  
  NotInstalledError(String m) { super(m); }
}