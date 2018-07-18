package com.obsidiandynamics.func;

/**
 *  A special case of {@link NullPointerException} that indicates that a {@code null}
 *  argument was supplied where a non-null reference was expected. Effectively, this
 *  is akin to an {@link IllegalArgumentException} for null arguments.
 */
public final class NullArgumentException extends NullPointerException {
  private static final long serialVersionUID = 1L;

  public NullArgumentException() {
    super();
  }

  public NullArgumentException(String m) {
    super(m);
  }
}
