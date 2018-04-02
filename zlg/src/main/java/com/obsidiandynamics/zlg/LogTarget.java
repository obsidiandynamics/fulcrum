package com.obsidiandynamics.zlg;

public interface LogTarget {
  boolean isEnabled(LogLevel level);
  
  void log(LogLevel level, String tag, String format, int argc, Object[] argv, Throwable throwable);
}
