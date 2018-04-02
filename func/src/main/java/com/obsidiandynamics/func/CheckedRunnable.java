package com.obsidiandynamics.func;

@FunctionalInterface
public interface CheckedRunnable<X extends Throwable> {
  void run() throws X;
  
  /**
   *  A no-op.
   */
  static void nop() {}
  
  static CheckedRunnable<RuntimeException> wrap(Runnable runnable) {
    return runnable::run;
  }
}