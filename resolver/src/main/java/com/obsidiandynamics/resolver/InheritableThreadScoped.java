package com.obsidiandynamics.resolver;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

final class InheritableThreadScoped implements Scoped {
  private final ThreadLocal<Map<Class<?>, Supplier<? extends Object>>> map = new InheritableThreadLocal<Map<Class<?>, Supplier<? extends Object>>>() {
    @Override
    protected Map<Class<?>, Supplier<? extends Object>> initialValue() {
      return new ConcurrentHashMap<>();
    }
  };
  
  @Override
  public Map<Class<?>, Supplier<? extends Object>> get() {
    return map.get();
  }
}
