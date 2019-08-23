package com.obsidiandynamics.worker;

import java.io.*;

@FunctionalInterface
public interface WorkerExceptionHandler {
  void handle(WorkerThread thread, Throwable exception);

  static WorkerExceptionHandler forPrintStream(PrintStream printStream) {
    return (thread, exception) -> {
      printStream.println("Exception in thread " + thread.getName());
      exception.printStackTrace(printStream);
    };
  }
  
  static WorkerExceptionHandler nop() {
    return (__thread, __exception) -> {};
  }
}
