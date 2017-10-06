package com.obsidiandynamics.shell;

import java.io.*;

/**
 *  The default process executor, delegating to {@link java.lang.ProcessBuilder}.
 */
public final class DefaultProcessExecutor implements ProcessExecutor {
  @Override
  public Process run(String... command) throws IOException {
    final ProcessBuilder pb = new ProcessBuilder(command);
    pb.redirectErrorStream(true);
    return pb.start();
  }
}