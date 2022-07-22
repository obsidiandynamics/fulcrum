package com.obsidiandynamics.func;

import java.io.*;
import java.util.function.*;

@FunctionalInterface
public interface LogLine extends Consumer<String> {
  static LogLine nop() {
    return __message -> {};
  }
  
  static LogLine forPrintStream(PrintStream stream) {
    return stream::println;
  }
  
  default void println(String message) {
    accept(message);
  }
  
  default void printf(String format, Object... args) {
    accept(String.format(format, args));
  }
}
