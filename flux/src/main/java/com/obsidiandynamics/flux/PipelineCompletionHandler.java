package com.obsidiandynamics.flux;

import java.io.*;

@FunctionalInterface
public interface PipelineCompletionHandler {
  void onComplete(Throwable error);
  
  static PipelineCompletionHandler nop() { return __ -> {}; }
  
  static PipelineCompletionHandler forPrintStream(PrintStream printStream) {
    return error -> {
      if (error != null) {
        printStream.println("Exception in pipeline");
        error.printStackTrace(printStream);
      }
    };
  }
}
