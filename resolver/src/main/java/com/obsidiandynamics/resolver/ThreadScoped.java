package com.obsidiandynamics.resolver;

import java.util.*;
import java.util.function.*;

/**
 *  Scopes the lookup context to a thread of execution, exhibiting
 *  behaviour analogous to a {@link ThreadLocal} (upon which this implementation
 *  is based).<p>
 *  
 *  {@link ThreadScoped} is the <em>default</em> scope used in {@link Resolver}.
 */
final class ThreadScoped implements Scoped {
  private final ThreadLocal<Map<Class<?>, Supplier<Object>>> map = ThreadLocal.withInitial(HashMap::new);
  
  @Override
  public Map<Class<?>, Supplier<Object>> map() {
    return map.get();
  }
}
