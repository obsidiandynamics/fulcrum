package com.obsidiandynamics.testmark;

import java.util.function.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.resolver.*;

public final class Testmark {
  @FunctionalInterface
  public interface LogLine extends Consumer<String> {}
  
  private static class TestmarkConfig {
    boolean enabled;
  }
  
  private Testmark() {}
  
  public final static class FluentTestmark {
    FluentTestmark() {}
    
    public <T> FluentTestmark withOptions(T options) {
      setOptions(options);
      return this;
    }
    
    public <T> FluentTestmark withOptions(Class<? super T> optionsType, T options) {
      setOptions(optionsType, options);
      return this;
    }
  }
  
  public static FluentTestmark enable() {
    Resolver.assign(TestmarkConfig.class, Singleton.of(new TestmarkConfig() {{
      enabled=true;
    }}));
    return new FluentTestmark();
  }
  
  public static void reset() {
    Resolver.reset(TestmarkConfig.class);
  }
  
  public static boolean isEnabled() {
    return Resolver.lookup(TestmarkConfig.class, TestmarkConfig::new).get().enabled;
  }
  
  public static <T> T getOptions(Class<? super T> optionsType, Supplier<T> optionsSupplier) {
    return Resolver.lookup(optionsType, optionsSupplier).get();
  }
  
  public static <T> void setOptions(T options) {
    @SuppressWarnings("unchecked")
    final Class<T> optionsType = (Class<T>) options.getClass();
    setOptions(optionsType, options);
  }
  
  public static <T> void setOptions(Class<? super T> optionsType, T options) {
    Resolver.assign(optionsType, Singleton.of(options));
  }
  
  public static <X extends Exception> void ifEnabled(CheckedRunnable<X> r) {
    ifEnabled(null, r);
  }
  
  static LogLine sysOut() {
    return System.out::println;
  }
  
  static ExceptionHandler sysErrExceptionHandler() {
    return ExceptionHandler.forPrintStream(System.err);
  }
  
  public static <X extends Exception> void ifEnabled(String name, CheckedRunnable<X> r) {
    if (isEnabled()) {
      final LogLine logLine = getOptions(LogLine.class, Testmark::sysOut);
      if (name != null) {
        logLine.accept(String.format("Starting benchmark (%s)...", name));
      } else {
        logLine.accept(String.format("Starting benchmark..."));
      }
      try {
        r.run();
      } catch (Throwable e) {
        final ExceptionHandler exceptionHandler = getOptions(ExceptionHandler.class, Testmark::sysErrExceptionHandler);
        exceptionHandler.onException("Unexpected error running benchmark", e);
      }
    }
  }
}
