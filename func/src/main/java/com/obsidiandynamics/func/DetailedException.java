package com.obsidiandynamics.func;

/**
 *  Used for decorating a {@link Throwable}, indicating that the latter carries a 'detailed'
 *  error object within.
 *
 *  @param <D> The detail type.
 */
@FunctionalInterface
public interface DetailedException<D> {
  D describeException();
}
