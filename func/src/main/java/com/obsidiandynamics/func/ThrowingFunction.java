package com.obsidiandynamics.func;

@FunctionalInterface
public interface ThrowingFunction<T, R> extends CheckedFunction<T, R, Exception> {}