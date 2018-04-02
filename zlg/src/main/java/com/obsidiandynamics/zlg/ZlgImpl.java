package com.obsidiandynamics.zlg;

final class ZlgImpl implements Zlg {
  static class DuplicateValueException extends IllegalStateException {
    private static final long serialVersionUID = 1L;
    DuplicateValueException(String m) { super(m); }
  }
  
  static class MissingValueException extends IllegalStateException {
    private static final long serialVersionUID = 1L;
    MissingValueException(String m) { super(m); }
  }
  
  static class TooManyArgsException extends IllegalStateException {
    private static final long serialVersionUID = 1L;
    TooManyArgsException(String m) { super(m); }
  }
  
  private class LogChainImpl implements LogChain {
    private LogLevel level;
    private String tag;
    private String format;
    private int argc;
    private Object[] argv = new Object[MAX_ARGS];
    private Throwable throwable;
    
    private void reset() {
      tag = null;
      format = null;
      for (int i = 0; i < argc; i++) {
        argv[i] = null;
      }
      argc = 0;
      throwable = null;
    }

    @Override
    public LogChain tag(String tag) {
      if (this.tag != null) throw new DuplicateValueException("Duplicate call to tag()");
      this.tag = tag;
      return this;
    }

    @Override
    public LogChain format(String format) {
      if (this.format != null) throw new DuplicateValueException("Duplicate call to format()");
      this.format = format;
      return this;
    }

    @Override
    public LogChain arg(boolean arg) {
      return appendArg(arg);
    }

    @Override
    public LogChain arg(byte arg) {
      return appendArg(arg);
    }

    @Override
    public LogChain arg(char arg) {
      return appendArg(arg);
    }

    @Override
    public LogChain arg(double arg) {
      return appendArg(arg);
    }

    @Override
    public LogChain arg(float arg) {
      return appendArg(arg);
    }

    @Override
    public LogChain arg(int arg) {
      return appendArg(arg);
    }

    @Override
    public LogChain arg(long arg) {
      return appendArg(arg);
    }

    @Override
    public LogChain arg(short arg) {
      return appendArg(arg);
    }

    @Override
    public LogChain arg(Object arg) {
      return appendArg(arg);
    }

    private LogChain appendArg(Object arg) {
      if (argc == MAX_ARGS) throw new TooManyArgsException("Number of args cannot exceed " + MAX_ARGS);
      argv[argc++] = arg;
      return this;
    }

    @Override
    public LogChain stack(Throwable throwable) {
      if (this.throwable != null) throw new DuplicateValueException("Duplicate call to exception()");
      this.throwable = throwable;
      return this;
    }

    @Override
    public void log() {
      if (format == null) throw new MissingValueException("Missing call to format()");
      target.log(level, tag, format, argc, argv, throwable);
      reset();
    }
  }
  
  private final LogLevel defaultLevel;
  
  private final LogTarget target;
  
  private final ThreadLocal<LogChainImpl> threadLocalChain = ThreadLocal.withInitial(LogChainImpl::new);
  
  ZlgImpl(String name, LogConfig config) {
    defaultLevel = config.getDefaultLevel();
    target = config.getLogService().get(name);
  }
  
  @Override
  public LogChain level(LogLevel level) {
    if (isEnabled(level)) {
      if (level == LogLevel.OFF) throw new IllegalArgumentException("Cannot log at level " + level.name());
      
      final LogChainImpl chain = threadLocalChain.get();
      chain.level = level;
      return chain;
    } else {
      return NopLevelChain.getInstance();
    }
  }
  
  @Override
  public boolean isEnabled(LogLevel level) {
    return level.sameOrHigherThan(defaultLevel) && target.isEnabled(level);
  }
}
