package com.obsidiandynamics.zlg;

public interface LogTarget {
  boolean isEnabled(LogLevel level);
  
  void log(LogLevel level, String tag, String message, int argc, Object[] argv, Throwable throwable);
}
