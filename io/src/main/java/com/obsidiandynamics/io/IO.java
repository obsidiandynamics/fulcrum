package com.obsidiandynamics.io;

import java.io.*;

import com.obsidiandynamics.func.*;

/**
 *  I/O utility methods.
 */
public final class IO {
  private IO() {}
  
  /**
   *  Closes a given {@link Closeable} instance, wrapping any potential {@link IOException}
   *  in an unchecked {@link RuntimeIOException}.
   *  
   *  @param closeable The {@link Closeable} to close.
   */
  public static void closeUnchecked(Closeable closeable) {
    Exceptions.wrapStrict(closeable::close, RuntimeIOException::new);
  }
}
