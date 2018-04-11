package com.obsidiandynamics.func;

import java.io.*;

@FunctionalInterface
public interface LogLine {
  static LogLine nop() {
    return __message -> {};
  }
  
  static LogLine forPrintStream(PrintStream stream) {
    return message -> stream.println(message);
  }
  
  void println(String message);
  
  default void printf(String format, Object... args) {
    println(String.format(format, args));
  }
}
