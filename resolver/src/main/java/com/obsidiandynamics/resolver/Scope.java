package com.obsidiandynamics.resolver;

import java.util.function.*;

public enum Scope {
  THREAD(ThreadScoped::new),
  INHERITABLE_THREAD(InheritableThreadScoped::new),
  THREAD_GROUP(ThreadGroupScoped::new);
  
  private final Supplier<Scoped> scopedMaker;
  
  private Scope(Supplier<Scoped> scopedMaker) {
    this.scopedMaker = scopedMaker;
  }

  Scoped make() {
    return scopedMaker.get();
  }
}
