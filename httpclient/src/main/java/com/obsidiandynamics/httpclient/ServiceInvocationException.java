package com.obsidiandynamics.httpclient;

public final class ServiceInvocationException extends Exception {
  private static final long serialVersionUID = 1L;

  ServiceInvocationException(String m, Throwable cause) { super(m, cause); }
}
