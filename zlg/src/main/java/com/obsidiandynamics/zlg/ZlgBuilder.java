package com.obsidiandynamics.zlg;

public final class ZlgBuilder {
  private static final LogConfig defaultConfigService = new LogConfig();
  
  private final String name;
  
  private ConfigService configService = defaultConfigService;

  ZlgBuilder(String name) {
    this.name = name;
  }
  
  ZlgBuilder withConfigService(ConfigService configService) {
    this.configService = configService;
    return this;
  }
  
  public Zlg get() {
    return new ZlgImpl(name, configService.get());
  }
}
