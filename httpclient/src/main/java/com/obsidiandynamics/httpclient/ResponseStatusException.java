package com.obsidiandynamics.httpclient;

import static com.obsidiandynamics.func.Functions.*;

/**
 *  Thrown by {@link HttpCall} if the response status does not match
 *  the caller's expectation.
 */
public final class ResponseStatusException extends Exception {
  private static final long serialVersionUID = 1L;
  
  private static final int MAX_ENTITY_DISPLAY_LENGTH = 20;
  
  private final int statusCode;
  
  private final String reasonPhrase;
  
  private final String entity;
  
  public ResponseStatusException(int statusCode, String reasonPhrase, String entity) {
    super("Unexpected response status: " + statusCode + 
          ", reason: " + reasonPhrase + 
          ", entity: " + ifPresent(entity, ResponseStatusException::abbreviateEntity));
    this.statusCode = statusCode;
    this.reasonPhrase = reasonPhrase;
    this.entity = entity;
  }
  
  private static String abbreviateEntity(String entity) {
    return "'" + abbreviate(entity, MAX_ENTITY_DISPLAY_LENGTH) + "'";
  }
  
  /**
   *  Truncates the given string at the specified length, using an ellipsis ('...')
   *  suffix to indicate that the string has been abbreviated.
   *  
   *  @param str The string to abbreviate.
   *  @param length The maximum length of the resulting string.
   *  @return The abbreviated {@link String}.
   */
  static String abbreviate(String str, int length) {
    final String ellipsis = "...";
    final int ellipsisLength = ellipsis.length();
    mustBeTrue(length >= ellipsisLength, withMessage(() -> "Length must not be shorter than " + ellipsisLength, 
                                                     IllegalArgumentException::new));
    return str.length() <= length ? str : str.substring(0, length - ellipsisLength).concat(ellipsis);
  }

  /**
   *  Obtains the status code.
   *  
   *  @return The response status code.
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   *  Obtains the reason phrase carried in the response.
   *  
   *  @return The reason phrase {@link String}, or {@code null} if the response had no reason.
   */
  public String getReasonPhrase() {
    return reasonPhrase;
  }
  
  /**
   *  Obtains the response entity string.
   *  
   *  @return The response entity {@link String}, or {@code null} if the response did not contain an entity.
   */
  public String getEntity() {
    return entity;
  }
}
