package com.obsidiandynamics.zlg;

@FunctionalInterface
public interface LogService {
  LogTarget create(String name);
}
