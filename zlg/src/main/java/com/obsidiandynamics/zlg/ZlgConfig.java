package com.obsidiandynamics.zlg;

public final class ZlgConfig {
  private static final LogService defaultLogService = new PrintStreamLogService(System.out);
  
  private LogLevel defaultLevel = LogLevel.CONF;
  
  private LogService logService = defaultLogService;

  LogLevel getDefaultLevel() {
    return defaultLevel;
  }

  public ZlgConfig withDefaultLevel(LogLevel defaultLevel) {
    this.defaultLevel = defaultLevel;
    return this;
  }

  LogService getLogService() {
    return logService;
  }

  public ZlgConfig withLogService(LogService logService) {
    this.logService = logService;
    return this;
  }

  @Override
  public String toString() {
    return ZlgConfig.class.getSimpleName() + " [defaultLevel=" + defaultLevel 
        + ", logService=" + logService + "]";
  }
}
