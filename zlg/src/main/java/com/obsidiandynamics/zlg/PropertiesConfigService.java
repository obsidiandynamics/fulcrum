package com.obsidiandynamics.zlg;

import java.net.*;
import java.util.*;

import com.obsidiandynamics.classes.*;
import com.obsidiandynamics.func.*;
import com.obsidiandynamics.io.*;
import com.obsidiandynamics.props.*;
import com.obsidiandynamics.threads.*;

public final class PropertiesConfigService implements ConfigService {
  public static final String KEY_DEFAULT_LEVEL = "zlg.default.level";
  public static final String KEY_LOG_SERVICE = "zlg.log.service";
  
  static final class ServiceInstantiationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    ServiceInstantiationException(Throwable cause) { super(cause); }
  }  
  
  static final class PropertiesLoadException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    PropertiesLoadException(Throwable cause) { super(cause); }
  }
  
  public interface PropertiesLoader extends ThrowingSupplier<Properties> {}
  
  private final PropertiesLoader propsLoader;
  
  private final Object cacheLock = new Object();
  
  private LogConfig cachedConfig;
  
  public PropertiesConfigService(PropertiesLoader propsLoader) {
    this.propsLoader = propsLoader;
  }

  @Override
  public LogConfig get() {
    synchronized (cacheLock) {
      if (cachedConfig == null) {
        final Properties props = Threads.wrap(propsLoader::get, PropertiesLoadException::new);
        cachedConfig = loadConfig(props);
      }
    }
    return cachedConfig;
  }
  
  private static LogConfig loadConfig(Properties props) {
    final LogLevel defaultLevel = Props.get(props, KEY_DEFAULT_LEVEL, LogLevel::valueOf, LogLevel.CONF);
    final String logServiceClassName = Props.get(props, KEY_LOG_SERVICE, String::valueOf, SysOutLogService.class.getName());
    final LogService logService = instantiateLogService(logServiceClassName);
    return new LogConfig()
        .withDefaultLevel(defaultLevel)
        .withLogService(logService);
  }
  
  private static LogService instantiateLogService(String logServiceClassName) {
    return Threads.wrap(() -> Classes.cast(Class.forName(logServiceClassName).getDeclaredConstructor().newInstance()), 
                        ServiceInstantiationException::new);
  }
  
  public static PropertiesLoader forUri(URI uri) {
    return () -> {
      final Properties props = new Properties();
      props.load(ResourceLoader.asStream(uri));
      return props;
    };
  }
}
