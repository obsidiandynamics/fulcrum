package com.obsidiandynamics.shell;

import java.io.*;

/**
 *  The default process executor, delegating to {@link java.lang.ProcessBuilder}.
 */
public final class DefaultProcessExecutor implements ProcessExecutor {
  private static final DefaultProcessExecutor instance = new DefaultProcessExecutor();
  
  public static DefaultProcessExecutor getInstance() { return instance; }
  
  private DefaultProcessExecutor() {}

  @Override
  public Process run(String[] command) throws IOException {
    final ProcessBuilder pb = new ProcessBuilder(command);
    pb.redirectErrorStream(true);
    return pb.start();
  }
}