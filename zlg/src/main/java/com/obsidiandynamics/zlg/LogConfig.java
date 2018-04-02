package com.obsidiandynamics.zlg;

public final class LogConfig implements ConfigService {
  private static final LogService defaultLogService = new SysOutLogService();
  
  private LogLevel defaultLevel = LogLevel.CONF;
  
  private LogService logService = defaultLogService;

  LogLevel getDefaultLevel() {
    return defaultLevel;
  }

  public LogConfig withDefaultLevel(LogLevel defaultLevel) {
    this.defaultLevel = defaultLevel;
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
    return LogConfig.class.getSimpleName() + " [defaultLevel=" + defaultLevel 
        + ", logService=" + logService + "]";
  }

  @Override
  public LogConfig get() {
    return this;
  }
}
