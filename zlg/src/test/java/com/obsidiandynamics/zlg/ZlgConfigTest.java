package com.obsidiandynamics.zlg;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class ZlgConfigTest {
  @Test
  public void test() {
    final LogLevel defaultLevel = LogLevel.DEBUG;
    final LogService logService = __name -> null;
    final LogConfig config = new LogConfig()
        .withDefaultLevel(defaultLevel)
        .withLogService(logService);
    assertEquals(defaultLevel, config.getDefaultLevel());
    assertEquals(logService, config.getLogService());
    Assertions.assertToStringOverride(config);
  }
}
