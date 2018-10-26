package com.obsidiandynamics.httpclient;

import java.io.*;
import java.util.concurrent.*;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.nio.client.*;
import org.apache.http.util.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.*;
import com.obsidiandynamics.json.*;

/**
 *  A builder for synchronous/blocking HTTP invocations backed by a {@link HttpAsyncClient}.
 */
public final class HttpCall {
  private final HttpAsyncClient client;
  
  private HttpCall(HttpAsyncClient client) {
    this.client = client;
  }
  
  public HttpCallResponse invoke(HttpUriRequest request) throws ServiceInvocationException, InterruptedException {
    return new HttpCallResponse(invoke(client, request));
  }

  public static HttpCall withClient(HttpAsyncClient client) {
    return new HttpCall(client);
  }
  
  public final class HttpCallResponse {
    private final HttpResponse response;
    
    private Json json = Json.getInstance();
    
    private Class<?> errorType = Object.class;

    HttpCallResponse(HttpResponse response) {
      this.response = response;
    }
    
    public HttpCallResponse withErrorType(Class<?> errorType) {
      this.errorType = errorType;
      return this;
    }
    
    public HttpCallResponse withJson(Json json) {
      this.json = json;
      return this;
    }
    
    public <T> T parseIfFound(Class<T> responseType) 
        throws ResponseStatusException, JsonInputException, IOException {
      return HttpCall.parseResponseIfFound(json, response, errorType, responseType);
    }
    
    public <T> T parseIfFound(Class<T> parametrizedResponseType, Class<?>... parameterClasses) 
        throws ResponseStatusException, JsonInputException, IOException {
      return HttpCall.parseResponseIfFound(json, response, errorType, parametrizedResponseType, parameterClasses);
    }
    
    public <T> T parse(Class<T> responseType) 
        throws JsonInputException, IOException, ResponseStatusException {
      return HttpCall.parseResponse(json, response, errorType, responseType);
    }

    public <T> T parse(Class<T> parametrizedResponseType, Class<?>... parameterClasses) 
        throws JsonInputException, IOException, ResponseStatusException {
      return HttpCall.parseResponse(json, response, parametrizedResponseType, parametrizedResponseType, parameterClasses);
    }
    
    public void ensureStatus(int expectedStatus) throws ResponseStatusException, IOException {
      HttpCall.ensureResponseStatus(json, response, expectedStatus, errorType);
    }
  }
  
  public static HttpResponse invoke(HttpAsyncClient client, HttpUriRequest request) 
      throws ServiceInvocationException, InterruptedException {
    try {
      return client.execute(request, null).get();
    } catch (ExecutionException e) {
      throw new ServiceInvocationException(e.getMessage(), e.getCause());
    }
  }
  
  public static <T> T parseResponseIfFound(Json json, 
                                           HttpResponse response, 
                                           Class<?> errorType, 
                                           Class<T> responseType) 
      throws ResponseStatusException, JsonInputException, IOException {
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
      return null;
    } else {
      return parseResponse(json, response, errorType, responseType);
    }
  }
  
  public static <T> T parseResponseIfFound(Json json, 
                                           HttpResponse response, 
                                           Class<?> errorType, 
                                           Class<T> parametrizedResponseType, 
                                           Class<?>... parameterClasses) 
      throws ResponseStatusException, JsonInputException, IOException {
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
      return null;
    } else {
      return parseResponse(json, response, errorType, parametrizedResponseType, parameterClasses);
    }
  }
  
  public static <T> T parseResponse(Json json, 
                                    HttpResponse response, 
                                    Class<?> errorType, 
                                    Class<T> responseType) 
      throws JsonInputException, IOException, ResponseStatusException {
    return parseResponse(json, response, errorType, TypeFactory.defaultInstance().constructType(responseType));
  }

  public static <T> T parseResponse(Json json,
                                    HttpResponse response, 
                                    Class<?> errorType, 
                                    Class<T> parametrizedResponseType, 
                                    Class<?>... parameterClasses) 
      throws JsonInputException, IOException, ResponseStatusException {
    final JavaType objectType = Json.typeOf(parametrizedResponseType, parameterClasses);
    return parseResponse(json, response, errorType, objectType);
  }

  private static <T> T parseResponse(Json json, 
                                     HttpResponse response, 
                                     Class<?> errorType, 
                                     JavaType responseType) 
      throws JsonInputException, IOException, ResponseStatusException {
    final int statusCode = response.getStatusLine().getStatusCode();
    final String entityString = parseEntityFromResponse(response);
    if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
      return json.parse(entityString, responseType);
    } else {
      throw createResponseStatusException(json, entityString, response.getStatusLine(), errorType);
    }
  }
  
  private static String parseEntityFromResponse(HttpResponse response) throws IOException, ResponseStatusException {
    final HttpEntity entity = response.getEntity();
    if (entity != null) {
      return parseEntity(entity);
    } else {
      throw new ResponseStatusException(response.getStatusLine(), null);
    }
  }
  
  public static String parseEntity(HttpEntity entity) throws IOException {
    try {
      return EntityUtils.toString(entity);
    } catch (ParseException e) {
      throw new IOException(e);
    }
  }
  
  public static void ensureResponseStatus(Json json, HttpResponse response, int expectedStatus, Class<?> errorType) 
      throws ResponseStatusException, IOException {
    if (response.getStatusLine().getStatusCode() != expectedStatus) {
      final String entityString = parseEntityFromResponse(response);
      throw createResponseStatusException(json, entityString, response.getStatusLine(), errorType);
    }
  }
  
  private static ResponseStatusException createResponseStatusException(Json json, 
                                                                       String entity, 
                                                                       StatusLine statusLine, 
                                                                       Class<?> errorType) {
    final String trimmedEntity = entity.trim();
    if (! trimmedEntity.isEmpty()) {
      try {
        final Object serviceError = json.parse(trimmedEntity, errorType);
        return new ResponseStatusException(statusLine, serviceError);
      } catch (JsonInputException e) {
        return new ResponseStatusException(statusLine, trimmedEntity);
      }
    } else {
      return new ResponseStatusException(statusLine, (Object) null);
    }
  }
}
