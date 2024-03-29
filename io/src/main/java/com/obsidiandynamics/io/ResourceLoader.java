package com.obsidiandynamics.io;

import java.io.*;
import java.net.*;

/**
 *  Loads file and classpath resources from a {@code URI}.
 */
public final class ResourceLoader {
  private ResourceLoader() {}
  
  /**
   *  Attempts to load a specified resource URI as an {@link InputStream}. The URI can be of the form 
   *  {@code file://...} - for reading files from the local file system, or {@code cp://...} 
   *  - for reading files from the classpath (e.g. if the file has been packaged into an
   *  application JAR). Returns {@code null} if the resource does not exist.
   *  
   *  @param uri The URI.
   *  @return The resulting input stream, or {@code null}.
   */
  public static InputStream tryStream(URI uri) {
    try {
      return stream(uri);
    } catch (FileNotFoundException e) {
      return null;
    }
  }
  
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
  public static InputStream stream(URI uri) throws FileNotFoundException {
    switch (uri.getScheme()) {
      case "file":
        return new FileInputStream(stripScheme(uri));
        
      case "cp":
      case "classpath":
        final InputStream in = ResourceLoader.class.getClassLoader().getResourceAsStream(stripScheme(uri));
        if (in != null) {
          return in;
        } else {
          throw new FileNotFoundException("No such resource '" + uri + "'");
        }
        
      default:
        throw new IllegalArgumentException("Unsupported URI scheme " + uri.getScheme());
    }
  }
  
  /**
   *  Removes the scheme and the {@code ://} separator from the URI. For example, the URI
   *  {@code file://dir/hello.txt} becomes {@code dir/hello.txt}.
   *  
   *  @param uri The URI to strip.
   *  @return The stripped string.
   */
  private static String stripScheme(URI uri) {
    return uri.toString().substring((uri.getScheme() + "://").length());
  }
}
