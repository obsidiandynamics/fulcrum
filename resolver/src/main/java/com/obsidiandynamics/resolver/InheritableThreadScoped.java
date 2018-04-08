package com.obsidiandynamics.resolver;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 *  Scopes the lookup context to a thread of execution and its child
 *  threads, analogous to {@link InheritableThreadLocal} (upon which this implementation
 *  is based).<p>
 *  
 *  Specifically, any lookup or assignment done in a parent thread is sufficient to initialise
 *  the backing context. Subsequent lookups and assignments across the initialising thread
 *  and any of its children (and their children, recursively) will share the same context.
 *  However, if a child thread performs a lookup or an assignment before the parent has had a
 *  chance to initialise the context, then the context will only be visible to that child
 *  (and its children), but not to the parent or its other siblings. (In this case, the parent
 *  and other siblings will have their own contexts.)
 */
final class InheritableThreadScoped implements Scoped {
  private final ThreadLocal<Map<Class<?>, Supplier<Object>>> map = new InheritableThreadLocal<Map<Class<?>, Supplier<Object>>>() {
    @Override
    protected Map<Class<?>, Supplier<Object>> initialValue() {
      return new ConcurrentHashMap<>();
    }
  };
  
  @Override
  public Map<Class<?>, Supplier<Object>> map() {
    return map.get();
  }
}
