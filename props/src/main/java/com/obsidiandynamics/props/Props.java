package com.obsidiandynamics.props;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.*;

public final class Props {
  private Props() {}
  
  public static <T> T get(String key, Function<String, T> parser, T defaultValue) {
    return get(System.getProperties(), key, parser, defaultValue);
  }
  
  public static <T> T get(Properties props, String key, Function<String, T> parser, T defaultValue) {
    final String str = props.getProperty(key);
    return str != null ? parser.apply(str) : defaultValue;
  }
  
  public static <T> T getOrSet(Properties props, String key, Function<String, T> parser, T defaultValue) {
    final String str = props.getProperty(key);
    if (str == null) {
      props.setProperty(key, String.valueOf(defaultValue));
      return defaultValue;
    } else {
      return parser.apply(str);
    }
  }
  
  public static Properties load(String resourceFile) throws IOException {
    final URL url = Props.class.getClassLoader().getResource(resourceFile);
    if (url == null) throw new FileNotFoundException("Resource not found");
    
    final Properties props = new Properties();
    try (InputStream in = url.openStream()) {
      props.load(in);
    }
    return props;
  }
  
  public static Properties load(String resourceFile, Properties defaultProps) {
    try {
      return load(resourceFile);
    } catch (IOException e) {
      return defaultProps;
    }
  }
  
  public static Properties merge(Properties... propertiesArray) {
    final Properties merged = new Properties();
    for (Properties props : propertiesArray) {
      Collections.list(props.propertyNames()).stream()
      .map(o -> (String) o)
      .forEach(key -> merged.put(key, props.getProperty(key)));
    }
    return merged;
  }
}
