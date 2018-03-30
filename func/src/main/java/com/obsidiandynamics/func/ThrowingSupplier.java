package com.obsidiandynamics.func;

@FunctionalInterface
public interface ThrowingSupplier<T> extends CheckedSupplier<T, Exception> {}