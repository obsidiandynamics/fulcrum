package com.obsidiandynamics.version;

import java.io.*;
import java.net.*;

public final class AppVersion {
  interface DefaultValueSupplier {
    String get() throws IOException;
  }
  
  static final class Constant implements DefaultValueSupplier {
    private final String def;
    
    Constant(String def) { this.def = def; }

    @Override
    public String get() throws FileNotFoundException {
      return def;
    }
  }
  
  private AppVersion() {}
  
  public static String get(String appName) throws IOException {
    return getFile(appName + ".version") + "_" + getFile(appName + ".build", new Constant("0"));
  }
    
  static String getFile(String versionFile) throws IOException {
    return getFile(versionFile, () -> {
      throw new FileNotFoundException("Not found: " + versionFile);
    });
  }
  
  static String getFile(String versionFile, DefaultValueSupplier defaultValueSupplier) throws IOException {
    return readResourceHead(versionFile, defaultValueSupplier);
  }
  
  private static String readResourceHead(String file, DefaultValueSupplier defaultValueSupplier) throws IOException {
    final URL url = AppVersion.class.getClassLoader().getResource(file);
    if (url == null) return defaultValueSupplier.get();
    
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
      return reader.readLine().trim();
    }
  }
}
