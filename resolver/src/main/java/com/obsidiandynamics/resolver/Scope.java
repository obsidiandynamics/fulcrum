package com.obsidiandynamics.resolver;

import java.util.function.*;

/**
 *  Constrains the scope of a lookup.
 */
public enum Scope {
  /**
   *  Scopes the lookup context to a thread of execution, exhibiting
   *  behaviour analogous to a {@link ThreadLocal} (upon which this implementation
   *  is based).
   */
  THREAD(ThreadScoped::new),
  
  /**
   *  Scopes the lookup context to a thread group, allowing related (by group) threads
   *  to share the same context.
   */
  THREAD_GROUP(ThreadGroupScoped::new);
  
  private final Supplier<Scoped> maker;
  
  Scope(Supplier<Scoped> maker) {
    this.maker = maker;
  }

  Scoped make() {
    return maker.get();
  }
}
