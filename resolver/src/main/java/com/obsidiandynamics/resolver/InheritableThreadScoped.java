package com.obsidiandynamics.resolver;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

final class InheritableThreadScoped implements Scoped {
  private final ThreadLocal<Map<Class<?>, Supplier<? extends Object>>> map = 
      InheritableThreadLocal.withInitial(ConcurrentHashMap::new);
  
  @Override
  public Map<Class<?>, Supplier<? extends Object>> get() {
    return map.get();
  }
}
