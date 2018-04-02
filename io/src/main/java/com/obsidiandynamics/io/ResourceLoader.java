package com.obsidiandynamics.io;

import java.io.*;
import java.net.*;

/**
 *  Loads file and classpath resources from a {@code URI}.
 */
public final class ResourceLoader {
  private ResourceLoader() {}
  
  /**
   *  Loads a specified resource URI as an {@link InputStream}. The URI can be of the form 
   *  {@code file://...} - for reading files from the local file system, or {@code cp://...} 
   *  - for reading files from the classpath (e.g. if the file has been packaged into an
   *  application JAR).
   *  
   *  @param uri The URI.
   *  @return The resulting input stream.
   *  @throws FileNotFoundException If the resource cannot be found.
   */
  public static InputStream asStream(URI uri) throws FileNotFoundException {
    switch (uri.getScheme()) {
      case "file":
        return new FileInputStream(new File(uri.getHost() + uri.getPath()));
        
      case "cp":
      case "classpath":
        return ResourceLoader.class.getClassLoader().getResourceAsStream(uri.getHost() + uri.getPath());
        
      default:
        throw new IllegalArgumentException("Unsupported URI scheme " + uri.getScheme());
    }
  }
}
