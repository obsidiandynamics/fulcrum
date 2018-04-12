package com.obsidiandynamics.shell;

import java.util.function.*;

/**
 *  Reads the output of the executed process (stdout + stderr). A sink may be
 *  invoked multiple times during the course of execution.
 */
@FunctionalInterface
public interface Sink extends Consumer<String> {
  static Sink nop() { return __ -> {}; }
}
