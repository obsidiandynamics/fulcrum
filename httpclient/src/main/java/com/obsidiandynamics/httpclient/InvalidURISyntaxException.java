package com.obsidiandynamics.httpclient;

public final class InvalidURISyntaxException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public InvalidURISyntaxException(Throwable cause) { 
    super(cause); 
  }
}