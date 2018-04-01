package com.obsidiandynamics.zlg;

public interface Zlg {
  interface LevelChain {
    LevelChain tag(String tag);
    
    LevelChain format(String format);
    
    LevelChain arg(boolean arg);
    
    LevelChain arg(byte arg);
    
    LevelChain arg(char arg);
    
    LevelChain arg(double arg);
    
    LevelChain arg(float arg);
    
    LevelChain arg(int arg);
    
    LevelChain arg(long arg);
    
    LevelChain arg(short arg);
    
    LevelChain arg(Object arg);
    
    LevelChain stack(Throwable throwable);
    
    void log();
  }
  
  LevelChain level(LogLevel level);
  
  boolean isEnabled(LogLevel level);
  
  default LevelChain e(String format) { return level(LogLevel.ERROR).format(format); }
  
  default LevelChain w(String format) { return level(LogLevel.WARN).format(format); }
  
  default LevelChain i(String format) { return level(LogLevel.INFO).format(format); }
  
  default LevelChain c(String format) { return level(LogLevel.CONF).format(format); }
  
  default LevelChain d(String format) { return level(LogLevel.DEBUG).format(format); }
  
  default LevelChain t(String format) { return level(LogLevel.TRACE).format(format); }
  
  static ZlgBuilder forName(String name) {
    return new ZlgBuilder(name);
  }
  
  static ZlgBuilder forClass(Class<?> cls) {
    return forName(cls.getName());
  }
}
