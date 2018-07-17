package com.obsidiandynamics.func;

@FunctionalInterface
public interface ThrowingConsumer<T> extends CheckedConsumer<T, Throwable> {}
