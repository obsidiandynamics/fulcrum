package com.obsidiandynamics.zlg;

import com.obsidiandynamics.zlg.Zlg.*;

final class NopLevelChain implements LevelChain {
  private static final NopLevelChain instance = new NopLevelChain();
  
  static NopLevelChain getInstance() { return instance; }
  
  private NopLevelChain() {}
  
  @Override
  public LevelChain tag(String tag) {
    return this;
  }

  @Override
  public LevelChain format(String format) {
    return this;
  }

  @Override
  public LevelChain arg(boolean arg) {
    return this;
  }

  @Override
  public LevelChain arg(byte arg) {
    return this;
  }

  @Override
  public LevelChain arg(char arg) {
    return this;
  }

  @Override
  public LevelChain arg(double arg) {
    return this;
  }

  @Override
  public LevelChain arg(float arg) {
    return this;
  }

  @Override
  public LevelChain arg(int arg) {
    return this;
  }

  @Override
  public LevelChain arg(long arg) {
    return this;
  }

  @Override
  public LevelChain arg(short arg) {
    return this;
  }

  @Override
  public LevelChain arg(Object arg) {
    return this;
  }

  @Override
  public LevelChain stack(Throwable throwable) {
    return this;
  }

  @Override
  public void log() {}
}
