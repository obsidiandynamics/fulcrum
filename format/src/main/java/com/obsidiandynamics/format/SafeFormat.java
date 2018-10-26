package com.obsidiandynamics.format;

import java.util.*;
import java.util.function.*;

/**
 *  An exception-safe wrapper for {@link String#format(String, Object...)} that is tolerant
 *  of incorrect format specifiers.
 */
public final class SafeFormat {
  private SafeFormat() {}
  
  public static Supplier<String> supply(String format, Object... args) {
    return () -> format(format, args);
  }
  
  public static String format(String format, Object... args) {
    return format(format, args.length, args);
  }
  
  public static String format(String format, int argc, Object[] argv) {
    try {
      return String.format(format, argv);
    } catch (Throwable e) {
      return "WARNING - could not format '" + format + "' with args " + Arrays.toString(copyArgs(argc, argv)) + ": " + e;
    }
  }
  
  public static Object[] copyArgs(int argc, Object[] argv) {
    final Object[] args = new Object[argc];
    System.arraycopy(argv, 0, args, 0, argc);
    return args;
  }
}