package com.obsidiandynamics.io;

import java.io.*;

/**
 *  A runtime variant of {@link IOException}.
 */
public final class RuntimeIOException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public RuntimeIOException(Throwable cause) { super(cause); }
}