package com.obsidiandynamics.fslock;

import java.io.*;

/**
 *  Produces a {@link Closeable} lock for a given {@link File}.
 */
@FunctionalInterface
public interface LockProvider {
  Closeable tryLock(File file) throws IOException;
}
