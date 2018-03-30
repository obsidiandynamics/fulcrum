package com.obsidiandynamics.func;

@FunctionalInterface
public interface ThrowingRunnable extends CheckedRunnable<Exception> {}