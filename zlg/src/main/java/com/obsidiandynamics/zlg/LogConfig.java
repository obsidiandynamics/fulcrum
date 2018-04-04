package com.obsidiandynamics.zlg;

public final class LogConfig implements ConfigService {
  private static final LogLevel defaultLevel = LogLevel.CONF;
  private static final LogService defaultLogService = new SysOutLogService();
  
  public static LogLevel getDefaultLevel() { return defaultLevel; }
  
  public static LogService getDefaultLogService() { return defaultLogService; }
  
  private LogLevel rootLevel = LogLevel.CONF;
  
  private LogService logService = defaultLogService;

  LogLevel getRootLevel() {
    return rootLevel;
  }

  public LogConfig withRootLevel(LogLevel rootLevel) {
    this.rootLevel = rootLevel;
    return this;
  }

  LogService getLogService() {
    return logService;
  }

  public LogConfig withLogService(LogService logService) {
    this.logService = logService;
    return this;
  }

  @Override
  public String toString() {
    return LogConfig.class.getSimpleName() + " [rootLevel=" + rootLevel 
        + ", logService=" + logService + "]";
  }

  @Override
  public LogConfig get() {
    return this;
  }
}
