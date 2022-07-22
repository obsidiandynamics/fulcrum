package com.obsidiandynamics.props;

import static java.lang.String.*;

import java.util.*;
import java.util.function.*;

import com.obsidiandynamics.func.*;

public final class PropsFormat {
  private PropsFormat() {}
  
  public static int measureKeyWidth(Properties props, Predicate<String> keyPredicate) {
    return Collections.list(props.propertyNames()).stream()
        .map(o -> (String) o)
        .filter(keyPredicate)
        .map(String::length)
        .max(Integer::compare).orElse(0);
  }
  
  public static void printStandard(LogLine logLine, 
                                   Properties props,
                                   int maxKeyPad, 
                                   String keyPrefix) {
    final Predicate<String> keyPredicate = startsWith(keyPrefix);
    final int rightPad = Math.max(maxKeyPad, measureKeyWidth(props, keyPredicate));
    printProps(logLine, props, rightPad(rightPad).andThen(prefix("- ")), prefix(" "), keyPredicate);
  }
  
  public static Function<String, String> leftPad(int chars) {
    return s -> format("%" + chars + "s", s);
  }
  
  public static Function<String, String> rightPad(int chars) {
    return s -> format("%-" + chars + "s", s);
  }
  
  public static Function<String, String> prefix(String prefix) {
    return s -> prefix + s;
  }
  
  public static Function<String, String> suffix(String suffix) {
    return s -> s + suffix;
  }
  
  public static Predicate<String> startsWith(String prefix) {
    return s -> s.startsWith(prefix);
  }
  
  public static Predicate<String> any() {
    return s -> true;
  }
  
  public static void printProps(LogLine logLine,
                                Properties props, 
                                Function<String, String> keyFormat,
                                Function<String, String> valueFormat,
                                Predicate<String> keyPredicate) {
    for (Enumeration<?> keys = props.propertyNames(); keys.hasMoreElements();) {
      final String key = (String) keys.nextElement();
      if (keyPredicate.test(key)) {
        logLine.println(keyFormat.apply(key) + ":" + valueFormat.apply(props.getProperty(key)));
      }
    }
  }
}
