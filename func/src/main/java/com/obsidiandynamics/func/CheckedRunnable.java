package com.obsidiandynamics.func;

/**
 *  A variant of {@link java.lang.Runnable} that is permitted to throw a checked
 *  exception.
 *  
 *  @param <X> Exception type.
 */
@FunctionalInterface
public interface CheckedRunnable<X extends Throwable> {
  void run() throws X;
  
  /**
   *  A no-op.
   */
  static void nop() {}
  
  /**
   *  Coerces a conventional {@link Runnable} to a {@link CheckedRunnable} that
   *  throws a {@link RuntimeException}.
   *  
   *  @param runnable The runnable to coerce.
   *  @return An equivalent {@link CheckedRunnable}.
   */
  static CheckedRunnable<RuntimeException> toChecked(Runnable runnable) {
    return runnable::run;
  }

  /**
   *  Coerces a {@link CheckedRunnable} that throws a {@link RuntimeException} 
   *  to a conventional {@link Runnable}.
   *  
   *  @param runnable The runnable to coerce.
   *  @return An equivalent {@link Runnable}.
   */
  static Runnable toUnchecked(CheckedRunnable<RuntimeException> runnable) {
    return runnable::run;
  }
}