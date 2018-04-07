package com.obsidiandynamics.resolver;

import java.util.*;
import java.util.function.*;

final class ThreadScoped implements Scoped {
  private final ThreadLocal<Map<Class<?>, Supplier<? extends Object>>> map = ThreadLocal.withInitial(HashMap::new);
  
  @Override
  public Map<Class<?>, Supplier<? extends Object>> get() {
    return map.get();
  }
}
