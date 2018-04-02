package com.obsidiandynamics.zlg;

@FunctionalInterface
public interface LogService {
  LogTarget get(String name);
}
