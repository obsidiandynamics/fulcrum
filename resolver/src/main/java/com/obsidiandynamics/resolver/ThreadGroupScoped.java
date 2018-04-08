package com.obsidiandynamics.resolver;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 *  Scopes the lookup context to a thread group, allowing related (by group) threads
 *  to share the same context.
 */
final class ThreadGroupScoped implements Scoped {
  private final Map<ThreadGroup, Map<Class<?>, Supplier<Object>>> groups = new WeakHashMap<>();
  
  private final Object lock = new Object();
  
  @Override
  public Map<Class<?>, Supplier<Object>> map() {
    synchronized (lock) {
      return groups.computeIfAbsent(Thread.currentThread().getThreadGroup(), __group -> new ConcurrentHashMap<>());
    }
  }
}
