package com.obsidiandynamics.func;

/**
 *  Used for decorating a {@link Throwable}, indicating that the latter carries a 'detailed'
 *  error object within.
 *
 *  @param <D> Detail type.
 */
@FunctionalInterface
public interface DetailedException<D> {
  D describeException();
}
