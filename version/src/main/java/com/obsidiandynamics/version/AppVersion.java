package com.obsidiandynamics.version;

import java.io.*;
import java.net.*;

/**
 *  Enables an application to read a version and build number stored in the
 *  application's classpath.<p>
 *  
 *  These artefacts are read by convention; for an application {@code foo}, the
 *  version number and buildstamp are expected to be in files {@code foo.version} and 
 *  {@code foo.build} respectively. The buildstamp may be omitted, in which case
 *  it assumes a set default value.
 */
public final class AppVersion {
  /**
   *  Provides the default value for a buildstamp, when the latter is missing
   *  from the classpath.
   */
  @FunctionalInterface
  public interface DefaultValueSupplier {
    String get() throws IOException;
  }
  
  /**
   *  A default value represented as a string constant.
   */
  public static final class Constant implements DefaultValueSupplier {
    private final String def;
    
    Constant(String def) { this.def = def; }

    @Override
    public String get() throws FileNotFoundException {
      return def;
    }
  }
  
  private AppVersion() {}
  
  /**
   *  This variant of {@link #get} obtains a concatenation of the application's version number
   *  and buildstamp, deferring to the constant {@code 0} if the optional buildstamp file is missing.
   *  
   *  @param appName The app name.
   *  @return The concatenated string.
   *  @throws IOException If an I/O error occurs.
   */
  public static String get(String appName) throws IOException {
    return get(appName, new Constant("0"));
  }
  
  /**
   *  Obtains a concatenation of the application's version number and buildstamp, in the form
   *  {@code version_build}. The version file <em>must</em> be present in the root classpath, and be
   *  named {@code appName.version}. The buildstamp file {@code appName.build} is optional; if
   *  the file is not found, the value returned by the {@code buildstampDefaultValueSupplier}
   *  is used in its place.
   *  
   *  @param appName The app name.
   *  @param buildstampDefaultValueSupplier Used if the buildstamp file is not found.
   *  @return The concatenated string.
   *  @throws IOException If an I/O error occurs.
   */
  public static String get(String appName, DefaultValueSupplier buildstampDefaultValueSupplier) throws IOException {
    return getFile(appName + ".version") + "_" + getFile(appName + ".build", buildstampDefaultValueSupplier);
  }
    
  static String getFile(String file) throws IOException {
    return getFile(file, () -> {
      throw new FileNotFoundException("Not found: " + file);
    });
  }
  
  static String getFile(String file, DefaultValueSupplier defaultValueSupplier) throws IOException {
    return readResourceHead(file, defaultValueSupplier);
  }
  
  private static String readResourceHead(String file, DefaultValueSupplier defaultValueSupplier) throws IOException {
    final URL url = AppVersion.class.getClassLoader().getResource(file);
    if (url == null) return defaultValueSupplier.get();
    
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
      return reader.readLine().trim();
    }
  }
}
