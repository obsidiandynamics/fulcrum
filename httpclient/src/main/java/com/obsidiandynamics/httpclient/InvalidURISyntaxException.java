package com.obsidiandynamics.httpclient;

public final class InvalidURISyntaxException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  InvalidURISyntaxException(Throwable cause) { super(cause); }
}