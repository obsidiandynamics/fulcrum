package com.obsidiandynamics.worker;

import java.io.*;

@FunctionalInterface
public interface WorkerExceptionHandler {
  void handle(WorkerThread thread, Throwable cause);

  static WorkerExceptionHandler forPrintStream(PrintStream printStream) {
    return (thread, cause) -> {
      printStream.println("Exception in thread " + thread.getName());
      cause.printStackTrace(printStream);
    };
  }
  
  static WorkerExceptionHandler nop() {
    return (__thread, __exception) -> {};
  }
}
