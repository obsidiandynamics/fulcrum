package com.obsidiandynamics.zlg;

public final class ZlgBuilder {
  private static final ZlgConfig defaultConfig = new ZlgConfig();
  
  private final String name;
  
  private ZlgConfig config = defaultConfig;

  ZlgBuilder(String name) {
    this.name = name;
  }
  
  public Zlg get() {
    return new ZlgImpl(name, config);
  }
}
