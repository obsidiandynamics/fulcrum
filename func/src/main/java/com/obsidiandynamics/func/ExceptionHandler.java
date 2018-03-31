package com.obsidiandynamics.func;

import java.io.*;

@FunctionalInterface
public interface ExceptionHandler {
  static ExceptionHandler nop() { return (__summary, __cause) -> {}; }
  
  static ExceptionHandler forPrintStream(PrintStream stream) {
    return (summary, cause) -> {
      if (summary != null) stream.println(summary);
      if (cause != null) cause.printStackTrace(stream);
    };
  }
  
  void onException(String summary, Throwable cause);
}
