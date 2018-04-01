package com.obsidiandynamics.zlg;

import java.io.*;

public final class PrintStreamLogService implements LogService {
  private final PrintStream stream;
  
  public PrintStreamLogService(PrintStream stream) {
    this.stream = stream;
  }

  @Override
  public LogTarget create(String name) {
    return new PrintStreamLogTarget(stream);
  }
}
