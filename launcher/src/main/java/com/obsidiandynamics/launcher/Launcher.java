package com.obsidiandynamics.launcher;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.props.*;
import com.obsidiandynamics.resolver.*;

public final class Launcher {
  @FunctionalInterface
  public interface ConsoleWriter {
    void printf(String format, Object ... args);
  }
  
  @FunctionalInterface
  public interface ConsoleReader {
    String readLine() throws IOException;
  }
  
  @FunctionalInterface
  public interface ClassRunner {
    void run(String className) throws Exception;
  }
  
  public static class Options {
    public int packageCompressLevel = Props.get("launcher.package.compress.level", Integer::parseInt, 0);
    
    public String partialClassName = Props.get("launcher.class", String::valueOf, null);
    
    public ConsoleWriter out = System.out::printf;
    
    public ConsoleWriter err = System.err::printf;
    
    public ConsoleReader in = getSystemInReader()::readLine;
    
    public ClassRunner runner = Launcher::run;
  }

  public static void run(Options options, String... classes) throws Exception {
    Arrays.sort(classes);
    if (options.partialClassName != null) {
      final String className = matchClass(options.partialClassName, classes);
      if (className != null) {
        options.out.printf("Running %s...\n", className);
        options.runner.run(className);
      } else {
        options.err.printf("Error: could not match class '%s'\n", options.partialClassName);
      }
    } else {
      options.out.printf("Select class to run\n");
      for (int i = 0; i < classes.length; i++) {
        options.out.printf("[%2d] %s\n", i + 1, formatClass(classes[i], options.packageCompressLevel));
      }
      
      final String read = options.in.readLine();
      if (read != null && ! read.trim().isEmpty()) {
        final int index;
        try {
          index = Integer.parseInt(read.trim()) - 1;
        } catch (NumberFormatException e) {
          options.err.printf("Invalid selection '%s'\n", read.trim());
          return;
        }
        if (index < 0 || index >= classes.length) {
          options.err.printf("Invalid selection '%s'\n", read.trim());
          return;
        }
        options.out.printf("Running %s...\n", classes[index]);
        options.runner.run(classes[index]);
      } else {
        options.out.printf("Exiting\n");
      }
    }
  }
  
  static String matchClass(String partialClassName, String[] classes) {
    return Arrays.stream(classes).filter(c -> c.endsWith(partialClassName)).findAny().orElse(null);
  }
  
  static String formatClass(String className, int packageCompressLevel) {
    final String[] frags = className.split("\\.");
    final StringBuilder formatted = new StringBuilder();
    for (int i = 0; i < frags.length; i++) {
      if (formatted.length() != 0) formatted.append('.');
      if (i < packageCompressLevel && i < frags.length - 1) {
        formatted.append(frags[i].charAt(0));
      } else {
        formatted.append(frags[i]);
      }
    }
    return formatted.toString();
  }
  
  static void run(String className) throws Exception {
    final Class<?> cls = Class.forName(className);
    final Method main = cls.getMethod("main", String[].class);
    main.invoke(null, (Object) new String[0]);
  }
  
  static BufferedReader getSystemInReader() {
    return new BufferedReader(new InputStreamReader(System.in));
  }
  
  static void runAndLogException(ThrowingRunnable runnable, PrintStream err) {
    try {
      runnable.run();
    } catch (Exception e) {
      err.printf("Error: %s\n", e);
      e.printStackTrace(err);
    }
  }
  
  private Launcher() {}
  
  public static void main(String[] args) {
    final Options options = Resolver.lookup(Options.class, Options::new).get();
    runAndLogException(() -> Launcher.run(options, args), System.err);
  }
}
