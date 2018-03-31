package com.obsidiandynamics.nanoclock;

public final class NanoClock {
  private static final long DIFF = System.currentTimeMillis() * 1_000_000L - System.nanoTime();
  
  private NanoClock() {}
  
  public static long now() {
    return System.nanoTime() + DIFF;
  }
}
