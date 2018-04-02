package com.obsidiandynamics.zlg;

import static com.obsidiandynamics.zlg.PropertiesConfigService.*;
import static org.junit.Assert.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.junit.*;

public final class PropertiesConfigServiceTest {
  @Test
  public void testLoadConfigSuccessAndCache() {
    final Properties props = new Properties();
    props.setProperty(KEY_DEFAULT_LEVEL, LogLevel.WARN.name());
    props.setProperty(KEY_LOG_SERVICE, SysOutLogService.class.getName());
    
    final PropertiesConfigService configService = new PropertiesConfigService(() -> props);
    final LogConfig config = configService.get();
    assertNotNull(config);
    assertEquals(LogLevel.WARN, config.getDefaultLevel());
    assertNotNull(config.getLogService());
    assertEquals(SysOutLogService.class, config.getLogService().getClass());
    
    final LogConfig config2 = configService.get();
    assertSame(config, config2);
  }
  
  @Test(expected=PropertiesLoadException.class)
  public void testLoadFailure() {
    new PropertiesConfigService(() -> { throw new Exception("simulated error"); }).get();
  }
  
  @Test(expected=FileNotFoundException.class)
  public void testClasspathLoadFailure() throws Exception {
    PropertiesConfigService.forUri(URI.create("file://nonexistentfile")).get();
  }
  
  @Test(expected=ServiceInstantiationException.class)
  public void testInstantiationFailure() {
    final Properties props = new Properties();
    props.setProperty(KEY_LOG_SERVICE, "nonexistentpackage.NonExistentClass");
    new PropertiesConfigService(() -> props).get();
  }
  
  @Test
  public void testFromFile() {
    final LogConfig config = new PropertiesConfigService(forUri(URI.create("cp://testconfig.properties"))).get();
    assertNotNull(config);
    assertEquals(LogLevel.WARN, config.getDefaultLevel());
    assertNotNull(config.getLogService());
    assertEquals(SysOutLogService.class, config.getLogService().getClass());
  }
}
