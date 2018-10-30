package com.obsidiandynamics.httpclient;

import static com.obsidiandynamics.func.Functions.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.function.*;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.nio.client.*;
import org.apache.http.util.*;

import com.obsidiandynamics.func.*;

/**
 *  A chained builder for synchronous/blocking HTTP invocations backed by 
 *  a {@link HttpAsyncClient}, supporting in-line status checks and response
 *  entity parsing.
 */
public final class HttpCall {
  private final HttpAsyncClient client;
  
  private HttpCall(HttpAsyncClient client) {
    this.client = client;
  }

  /**
   *  Begins an invocation chain using the supplied HTTP client.
   *  
   *  @param client The client to use for the subsequent invocation.
   *  @return The {@link HttpCall} object.
   */
  public static HttpCall withClient(HttpAsyncClient client) {
    return new HttpCall(client);
  }
  
  /**
   *  Issues the given request, blocking until either the client returns,
   *  the request times out, or the calling thread is interrupted.
   *  
   *  @param request The HTTP request to issue.
   *  @return The resulting {@link HttpCallResponse} object.
   *  @throws InterruptedException If the calling thread was interrupted.
   *  @throws IOException If an I/O error occurs. (This also includes {@link ConnectException} and
   *                      {@link SocketTimeoutException}.)
   */
  public HttpCallResponse invoke(HttpUriRequest request) throws InterruptedException, IOException {
    return new HttpCallResponse(invoke(client, request));
  }
  
  /**
   *  The result of a blocking HTTP call.
   */
  public final class HttpCallResponse {
    public final class ParseConditionally {
      private final Predicate<HttpResponse> predicate;

      ParseConditionally(Predicate<HttpResponse> predicate) {
        this.predicate = predicate;
      }
      
      /**
       *  Parses the entity string (which may be {@code null}) using the supplied {@code entityParser}
       *  function, assuming that the predicate used to chain this block passed. Otherwise, if the
       *  predicate failed, a {@code null} is returned.
       *  
       *  @param <T> Parsed type.
       *  @param <X> Exception type.
       *  @param entityParser The parser to use.
       *  @return The parsed entity object, or {@code null} if the predicate failed.
       *  @throws IOException If an I/O error occurs.
       *  @throws X If an error occurs in the parser function.
       */
      public <T, X extends Throwable> T parse(CheckedFunction<String, T, X> entityParser) throws IOException, X {
        if (predicate.test(response)) {
          return entityParser.apply(getEntityString());
        } else {
          return null;
        }
      }
    }
    
    private final HttpResponse response;
    
    HttpCallResponse(HttpResponse response) {
      this.response = response;
    }
    
    /**
     *  Obtains the underlying HTTP response object.
     *  
     *  @return The underlying {@link HttpResponse}.
     */
    public HttpResponse getResponse() {
      return response;
    }
    
    /**
     *  Obtains the status code of the response.
     *  
     *  @return The status code.
     */
    public int getStatusCode() {
      return response.getStatusLine().getStatusCode();
    }
    
    /**
     *  Obtains the reason phrase carried in the response, if one is set.
     *  
     *  @return The reason phrase {@link String}, possibly {@code null}.
     */
    public String getReasonPhrase() {
      return response.getStatusLine().getReasonPhrase();
    }
    
    /**
     *  Obtains the response entity-body.
     *  
     *  @return The response entity as a {@link String}.
     *  @throws IOException If an I/O error occurs.
     */
    public String getEntityString() throws IOException {
      return HttpCall.getEntityString(response);
    }
    
    /**
     *  Verifies that the response status code matches the expected status code, throwing
     *  a {@link ResponseStatusException} otherwise.
     *  
     *  @param expectedStatus The expected status code.
     *  @return This {@link HttpCallResponse} instance for chaining.
     *  @throws ResponseStatusException If the status code doesn't match the expected code.
     *  @throws IOException If an I/O error occurs.
     */
    public HttpCallResponse ensureStatusIs(int expectedStatus) throws ResponseStatusException, IOException {
      HttpCall.ensureResponseStatusIs(response, expectedStatus);
      return this;
    }
    
    /**
     *  Verifies that the response status code matches one of the expected status codes, throwing
     *  a {@link ResponseStatusException} otherwise.
     *  
     *  @param expectedStatuses An array of status codes to match.
     *  @return This {@link HttpCallResponse} instance for chaining.
     *  @throws ResponseStatusException If the status code doesn't match one of the expected codes.
     *  @throws IOException If an I/O error occurs.
     */
    public HttpCallResponse ensureStatusIs(int... expectedStatuses) throws ResponseStatusException, IOException {
      HttpCall.ensureResponseStatusIs(response, expectedStatuses);
      return this;
    }
    
    /**
     *  A convenience method for ensuring that the response status is {@code 200 OK}.
     *  
     *  @return This {@link HttpCallResponse} instance for chaining.
     *  @throws ResponseStatusException If the status code doesn't match the expected code.
     *  @throws IOException If an I/O error occurs.
     */
    public HttpCallResponse ensureIsOk() throws ResponseStatusException, IOException {
      return ensureStatusIs(HttpStatus.SC_OK);
    }
    
    /**
     *  A convenience method for ensuring that the response status is either {@code 200 OK}
     *  or {@code 201 Created}.
     *  
     *  @return This {@link HttpCallResponse} instance for chaining.
     *  @throws ResponseStatusException If the status code doesn't match one of the expected codes.
     *  @throws IOException If an I/O error occurs.
     */
    public HttpCallResponse ensureIsOkOrCreated() throws ResponseStatusException, IOException {
      return ensureStatusIs(HttpStatus.SC_OK, HttpStatus.SC_CREATED);
    }
    
    /**
     *  A convenience method for ensuring that the response status is either {@code 200 OK}
     *  or {@code 404 Not Found}.
     *  
     *  @return This {@link HttpCallResponse} instance for chaining.
     *  @throws ResponseStatusException If the status code doesn't match one of the expected codes.
     *  @throws IOException If an I/O error occurs.
     */
    public HttpCallResponse ensureIsOkOrNotFound() throws ResponseStatusException, IOException {
      return ensureStatusIs(HttpStatus.SC_OK, HttpStatus.SC_NOT_FOUND);
    }
    
    /**
     *  Parses the entity string (which may be {@code null}) using the supplied {@code entityParser}
     *  function, returning its object representation.
     *  
     *  @param <T> Parsed type.
     *  @param <X> Exception type.
     *  @param entityParser The parser to use.
     *  @return The parsed entity object.
     *  @throws IOException If an I/O error occurs.
     *  @throws X If an error occurs in the parser function.
     */
    public <T, X extends Throwable> T parse(CheckedFunction<String, T, X> entityParser) throws IOException, X {
      return entityParser.apply(getEntityString());
    }
    
    /**
     *  Prepares for the conditional parsing of the response entity if the underlying response matches
     *  the given predicate.
     *  
     *  @param predicate The predicate for testing the response.
     *  @return The {@link ParseConditionally} step.
     */
    public ParseConditionally ifResponse(Predicate<HttpResponse> predicate) {
      return new ParseConditionally(predicate);
    }
    
    /**
     *  Prepares for the conditional parsing of the response entity if the underlying response status
     *  code matches the expected status.
     *  
     *  @param expectedStatus The expected status code.
     *  @return The {@link ParseConditionally} step.
     */
    public ParseConditionally ifStatusIs(int expectedStatus) {
      return ifResponse(statusIs(expectedStatus));
    }
    
    /**
     *  Prepares for the conditional parsing of the response entity if the underlying response status
     *  code matches one of the expected statuses.
     *  
     *  @param expectedStatuses An array of status codes to match.
     *  @return The {@link ParseConditionally} step.
     */
    public ParseConditionally ifStatusIs(int... expectedStatuses) {
      return ifResponse(statusIs(expectedStatuses));
    }
    
    /**
     *  Prepares for the conditional parsing of the response entity if the underlying response status
     *  code is {@code 200 OK}. <p>
     *  
     *  This is conveniently used when a HTTP GET request may return either a
     *  {@code 200 OK} or a {@code 404 Not Found}, and parsing of the entity should only be attempted
     *  if the resource was found.
     *  
     *  @return The {@link ParseConditionally} step.
     */
    public ParseConditionally ifOk() {
      return ifStatusIs(HttpStatus.SC_OK);
    }
  }
  
  /**
   *  Uses the supplied client to issue the given request, blocking until either the client returns,
   *  the request times out, or the calling thread is interrupted.
   *  
   *  @param client The HTTP client to use.
   *  @param request The HTTP request to issue.
   *  @return The resulting {@link HttpResponse} object.
   *  @throws InterruptedException If the calling thread was interrupted.
   *  @throws IOException If an I/O error occurs. (This also includes {@link ConnectException} and
   *                      {@link SocketTimeoutException}.)
   */
  public static HttpResponse invoke(HttpAsyncClient client, HttpUriRequest request) throws InterruptedException, IOException {
    try {
      return client.execute(request, null).get();
    } catch (ExecutionException e) {
      throw Classes.coerce(e.getCause(), IOException.class, IOException::new);
    }
  }
  
  /**
   *  Obtains an entity string from the response if one is present.
   *  
   *  @param response The HTTP response.
   *  @return The entity {@link String}, or {@code null} if no entity is set.
   *  @throws IOException If an I/O error occurs.
   */
  public static String getEntityString(HttpResponse response) throws IOException {
    return ifPresent(response.getEntity(), HttpCall::toString);
  }
  
  /**
   *  Parses a given entity to a string.
   *  
   *  @param entity The HTTP entity.
   *  @return The parsed {@link String}.
   *  @throws IOException If an I/O error occurs.
   */
  public static String toString(HttpEntity entity) throws IOException {
    mustExist(entity, NullArgumentException::new);
    try {
      return EntityUtils.toString(entity);
    } catch (ParseException e) {
      throw new IOException(e);
    }
  }
  
  /**
   *  Verifies that the response status code matches the expected status code, throwing
   *  a {@link ResponseStatusException} otherwise.
   *  
   *  @param response The HTTP response.
   *  @param expectedStatus The expected status code.
   *  @throws ResponseStatusException If the status code doesn't match the expected code.
   *  @throws IOException If an I/O error occurs.
   */
  public static void ensureResponseStatusIs(HttpResponse response, int expectedStatus) 
      throws ResponseStatusException, IOException {
    if (! statusIs(expectedStatus).test(response)) {
      throw createResponseStatusException(response);
    }
  }
  
  /**
   *  Verifies that the response status code matches one of the expected status codes, throwing
   *  a {@link ResponseStatusException} otherwise.
   *  
   *  @param response The HTTP response.
   *  @param expectedStatuses An array of status codes to match.
   *  @throws ResponseStatusException If the status code doesn't match one of the expected codes.
   *  @throws IOException If an I/O error occurs.
   */
  public static void ensureResponseStatusIs(HttpResponse response, int... expectedStatuses) 
      throws ResponseStatusException, IOException {
    if (! statusIs(expectedStatuses).test(response)) {
      throw createResponseStatusException(response);
    }
  }
  
  private static ResponseStatusException createResponseStatusException(HttpResponse response) throws IOException {
    return new ResponseStatusException(response.getStatusLine().getStatusCode(), 
                                       response.getStatusLine().getReasonPhrase(),
                                       getEntityString(response));
  }
  
  private static Predicate<HttpResponse> statusIs(int expected) {
    return response -> response.getStatusLine().getStatusCode() == expected;
  }
  
  private static Predicate<HttpResponse> statusIs(int[] expected) {
    return response -> isIn(response.getStatusLine().getStatusCode(), expected);
  }
  
  private static boolean isIn(int actual, int[] expected) {
    for (int x : expected) {
      if (x == actual) {
        return true;
      }
    }
    return false;
  }
}
