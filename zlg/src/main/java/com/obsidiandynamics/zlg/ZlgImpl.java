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
  
  static int MAX_ARGS = 8;
  
  private class LevelChainImpl implements LevelChain {
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
    public LevelChain tag(String tag) {
      if (this.tag != null) throw new DuplicateValueException("Duplicate call to tag()");
      this.tag = tag;
      return this;
    }

    @Override
    public LevelChain format(String format) {
      if (this.format != null) throw new DuplicateValueException("Duplicate call to format()");
      this.format = format;
      return this;
    }

    @Override
    public LevelChain arg(boolean arg) {
      return appendArg(arg);
    }

    @Override
    public LevelChain arg(byte arg) {
      return appendArg(arg);
    }

    @Override
    public LevelChain arg(char arg) {
      return appendArg(arg);
    }

    @Override
    public LevelChain arg(double arg) {
      return appendArg(arg);
    }

    @Override
    public LevelChain arg(float arg) {
      return appendArg(arg);
    }

    @Override
    public LevelChain arg(int arg) {
      return appendArg(arg);
    }

    @Override
    public LevelChain arg(long arg) {
      return appendArg(arg);
    }

    @Override
    public LevelChain arg(short arg) {
      return appendArg(arg);
    }

    @Override
    public LevelChain arg(Object arg) {
      return appendArg(arg);
    }

    private LevelChain appendArg(Object arg) {
      if (argc == MAX_ARGS) throw new TooManyArgsException("Number of args cannot exceed " + MAX_ARGS);
      argv[argc++] = arg;
      return this;
    }

    @Override
    public LevelChain stack(Throwable throwable) {
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
  
  private final ThreadLocal<LevelChainImpl> threadLocalChain = ThreadLocal.withInitial(LevelChainImpl::new);
  
  ZlgImpl(String name, ZlgConfig config) {
    defaultLevel = config.getDefaultLevel();
    target = config.getLogService().create(name);
  }
  
  @Override
  public LevelChain level(LogLevel level) {
    if (isEnabled(level)) {
      final LevelChainImpl chain = threadLocalChain.get();
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
