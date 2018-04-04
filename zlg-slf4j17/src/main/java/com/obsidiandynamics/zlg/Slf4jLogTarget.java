package com.obsidiandynamics.zlg;

import org.slf4j.*;
import org.slf4j.spi.*;

final class Slf4jLogTarget implements LogTarget {
  private static String FQCN = ZlgImpl.LogChainImpl.class.getName();
  
  @FunctionalInterface
  private interface LogEnabled {
    boolean isEnabled();
  }
  
  @FunctionalInterface
  private interface LogMessage {
    void log(String message);
  }

  @FunctionalInterface
  private interface LogMarkerMessage {
    void log(Marker marker, String message);
  }

  @FunctionalInterface
  private interface LogMessageThrowable {
    void log(String message, Throwable throwable);
  }

  @FunctionalInterface
  private interface LogMarkerMessageThrowable {
    void log(Marker marker, String message, Throwable throwable);
  }
  
  private static class LogMapping {
    private final int intLevel;
    private final LogEnabled logEnabled;
    private final LogMessage logMessage;
    private final LogMarkerMessage logMarkerMessage;
    private final LogMessageThrowable logMessageThrowable;
    private final LogMarkerMessageThrowable logMarkerMessageThrowable;
    
    private LogMapping(int intLevel, LogEnabled logEnabled, LogMessage logMessage, LogMarkerMessage logMarkerMessage,
                       LogMessageThrowable logMessageThrowable, LogMarkerMessageThrowable logMarkerMessageThrowable) {
      this.intLevel = intLevel;
      this.logEnabled = logEnabled;
      this.logMessage = logMessage;
      this.logMarkerMessage = logMarkerMessage;
      this.logMessageThrowable = logMessageThrowable;
      this.logMarkerMessageThrowable = logMarkerMessageThrowable;
    }
    
    static LogMapping forLevel(Logger log, LogLevel level) {
      switch (level) {
        case ERROR:
          return new LogMapping(LocationAwareLogger.ERROR_INT,
                                log::isErrorEnabled,
                                log::error,
                                log::error,
                                log::error,
                                log::error);
          
        case WARN:
          return new LogMapping(LocationAwareLogger.WARN_INT,
                                log::isWarnEnabled,
                                log::warn,
                                log::warn,
                                log::warn,
                                log::warn);
          
        case INFO:
        case CONF:
          return new LogMapping(LocationAwareLogger.INFO_INT,
                                log::isInfoEnabled,
                                log::info,
                                log::info,
                                log::info,
                                log::info);
          
        case DEBUG:
          return new LogMapping(LocationAwareLogger.DEBUG_INT,
                                log::isDebugEnabled,
                                log::debug,
                                log::debug,
                                log::debug,
                                log::debug);
          
        case TRACE:
          return new LogMapping(LocationAwareLogger.TRACE_INT,
                                log::isTraceEnabled,
                                log::trace,
                                log::trace,
                                log::trace,
                                log::trace);
        
        case OFF:
        default:
          throw new UnsupportedOperationException("Unsupported level " + level);
      }
    }
  }
  
  private final boolean isLocationAware;
  
  private final Logger log;
  
  private final LogMapping mappings[] = new LogMapping[LogLevel.values().length];

  Slf4jLogTarget(Logger log) {
    this.log = log;
    isLocationAware = log instanceof LocationAwareLogger;
    
    for (LogLevel level : LogLevel.values()) {
      if (level != LogLevel.OFF) {
        mappings[level.ordinal()] = LogMapping.forLevel(log, level);
      }
    }
  }

  @Override
  public boolean isEnabled(LogLevel level) {
    return mappings[level.ordinal()].logEnabled.isEnabled();
  }
  
  private static final Object[] noArgs = {};

  @Override
  public void log(LogLevel level, String tag, String format, int argc, Object[] argv, Throwable throwable) {
    if (isLocationAware) {
      logWithLocation(level, tag, format, argc, argv, throwable);
    } else {
      logDirect(level, tag, format, argc, argv, throwable);
    }
  }
  
  private void logDirect(LogLevel level, String tag, String format, int argc, Object[] argv, Throwable throwable) {
    final LogMapping mapping = mappings[level.ordinal()];
    final String message = String.format(format, argv);
    if (tag != null && throwable != null) {
      final Marker marker = MarkerFactory.getMarker(tag);
      mapping.logMarkerMessageThrowable.log(marker, message, throwable);
    } else if (tag != null) {
      final Marker marker = MarkerFactory.getMarker(tag);
      mapping.logMarkerMessage.log(marker, message);
    } else if (throwable != null) {
      mapping.logMessageThrowable.log(message, throwable);
    } else {
      mapping.logMessage.log(message);
    }
  }

  private void logWithLocation(LogLevel level, String tag, String format, int argc, Object[] argv, Throwable throwable) {
    final Marker marker = tag != null ? MarkerFactory.getMarker(tag) : null;
    final String message = String.format(format, argv);
    final int intLevel = mappings[level.ordinal()].intLevel;
    ((LocationAwareLogger) log).log(marker, FQCN, intLevel, message, noArgs, throwable);
  }
}
