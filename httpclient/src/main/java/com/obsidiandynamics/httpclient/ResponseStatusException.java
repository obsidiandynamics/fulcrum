package com.obsidiandynamics.httpclient;

import org.apache.http.*;

import com.obsidiandynamics.func.*;

public final class ResponseStatusException extends Exception {
  private static final long serialVersionUID = 1L;
  
  private final int statusCode;
  
  private final String reasonPhrase;
  
  private final Object error;
  
  ResponseStatusException(StatusLine statusLine, Object error) {
    this(statusLine.getStatusCode(), statusLine.getReasonPhrase(), error, null);
  }
  
  private ResponseStatusException(int statusCode, String reasonPhrase, Object error, Throwable cause) {
    super("Unexpected response status " + statusCode + ", reason: " + reasonPhrase + ", error: " + error, cause);
    this.statusCode = statusCode;
    this.reasonPhrase = reasonPhrase;
    this.error = error;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getReasonPhrase() {
    return reasonPhrase;
  }
  
  public <T> T getError() {
    return Classes.cast(error);
  }
}
