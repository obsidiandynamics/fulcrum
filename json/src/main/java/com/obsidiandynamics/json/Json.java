package com.obsidiandynamics.json;

import java.io.*;
import java.util.*;
import java.util.function.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.*;
import com.obsidiandynamics.format.*;
import com.obsidiandynamics.func.*;

/**
 *  Wrapper around {@code com.fasterxml.jackson} APIs for working with (parsing and 
 *  formatting) JSON documents. <p>
 *  
 *  A {@link Json} singleton instance is provided (obtainable via
 *  {@link #getInstance()}), allowing core application classes to use a sensibly configured
 *  parser/formatter without instantiating their own.
 */
public final class Json {
  private static final Json instance = new Json(createDefaultMapper());
  
  /**
   *  Creates a default {@link ObjectMapper} configuration instance, as used by the singleton
   *  {@link Json} instance.
   *  
   *  @return A new {@link ObjectMapper} with the default configuration applied to it.
   */
  public static ObjectMapper createDefaultMapper() {
    final ObjectMapper mapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true)
        .setSerializationInclusion(Include.NON_NULL);
    
    mapper
    .configOverride(Date.class)
    .setFormat(JsonFormat.Value.forPattern(Iso8601.DATE_TIME_MILLIS_FORMAT));
    
    return mapper;
  }
  
  /*
   *  Obtains the singleton {@link Json} instance. This instance comes
   *  configured with sensible defaults, for example, the use of ISO 8601 for marshaling
   *  dates. <p>
   *  
   *  This instance is designed to be shared by application classes that benefit from a sensibly
   *  configured {@link Json} singleton, without the need to change the underlying 
   *  {@link ObjectMapper} configuration.
   */
  public static Json getInstance() {
    return instance;
  }
  
  private final ObjectMapper mapper;
  
  public Json(ObjectMapper mapper) {
    this.mapper = mapper;
  }
  
  /**
   *  Obtains the underlying object mapper.
   *  
   *  @return The backing {@link ObjectMapper} instance.
   */
  public ObjectMapper getMapper() {
    return mapper;
  }
  
  /**
   *  Parses a given JSON document of an expected {@link Class} type.
   *  
   *  @param <T> Object type.
   *  @param json The JSON document.
   *  @param objectType The target object type.
   *  @return The parsed object.
   *  @throws JsonInputException If an error occurs during parsing.
   */
  public <T> T parse(String json, Class<T> objectType) throws JsonInputException {
    return parse(json, typeOf(objectType));
  }
  
  /**
   *  Parses a given JSON document of an expected {@link JavaType} complex type token. <p>
   *  
   *  Use {@link #typeOf(Class)} and {@link #typeOf(Class, Class...)} to construct
   *  {@link JavaType} tokens from simple and parametrised types.
   *  
   *  @param <T> Object type.
   *  @param json The JSON document.
   *  @param objectType The target object type.
   *  @return The parsed object.
   *  @throws JsonInputException If an error occurs during parsing.
   */
  public <T> T parse(String json, JavaType objectType) throws JsonInputException {
    return parse(json, objectType, JsonInputException::new);
  }
  
  /**
   *  A variant of {@link #parse(String, JavaType)} that throws a custom exception type
   *  upon encountering a parse error.
   *  
   *  @param <T> Object type.
   *  @param <X> Custom exception type.
   *  @param json The JSON document.
   *  @param objectType The target object type.
   *  @param exceptionWrapper Maps an I/O exception thrown during parsing to a custom exception type.
   *  @return The parsed object.
   *  @throws X If an error occurs during parsing.
   */
  public <T, X extends Throwable> T parse(String json, JavaType objectType, 
                                          Function<? super IOException, X> exceptionWrapper) throws X {
    try {
      return mapper.readValue(json, objectType);
    } catch (IOException e) {
      throw exceptionWrapper.apply(e);
    }
  }
  
  /**
   *  An unchecked variant of {@link #parse(String, Class)}.
   *  
   *  @param <T> Object type.
   *  @param json The JSON document.
   *  @param objectType The target object type.
   *  @return The parsed object.
   */
  public <T> T parseUnchecked(String json, Class<T> objectType) {
    return parse(json, typeOf(objectType), RuntimeJsonException::new);
  }
  
  /**
   *  An unchecked variant of {@link #parse(String, JavaType)}. <p>
   *  
   *  Use {@link #typeOf(Class)} and {@link #typeOf(Class, Class...)} to construct
   *  {@link JavaType} tokens from simple and parametrised types.
   *  
   *  @param <T> Object type.
   *  @param json The JSON document.
   *  @param objectType The target object type.
   *  @return The parsed object.
   */
  public <T> T parseUnchecked(String json, JavaType objectType) {
    return parse(json, objectType, RuntimeJsonException::new);
  }
  
  /**
   *  Obtains a parser for the given {@link Class} type, being a 
   *  checked function that delegates to {@link #parse(String, Class)} when invoked.
   *  
   *  @param <T> Object type.
   *  @param objectType The target object type.
   *  @return The parser {@link CheckedFunction}.
   */
  public <T> CheckedFunction<String, T, JsonInputException> parser(Class<T> objectType) {
    return json -> parse(json, objectType);
  }
  
  /**
   *  Obtains a parser for the given {@link JavaType} complex type token, being a 
   *  checked function that delegates to {@link #parse(String, JavaType)} when invoked.
   *  
   *  @param <T> Object type.
   *  @param objectType The target object type.
   *  @return The parser {@link CheckedFunction}.
   */
  public <T> CheckedFunction<String, T, JsonInputException> parser(JavaType objectType) {
    return json -> parse(json, objectType);
  }
  
  /**
   *  Obtains a parser for the given {@link Class} type, being a 
   *  checked function that delegates to {@link #parseUnchecked(String, Class)} when invoked.
   *  
   *  @param <T> Object type.
   *  @param objectType The target object type.
   *  @return The parser {@link Function}.
   */
  public <T> Function<String, T> uncheckedParser(Class<T> objectType) {
    return json -> parseUnchecked(json, objectType);
  }
  
  /**
   *  Obtains a parser for the given {@link JavaType} complex type token, being a 
   *  checked function that delegates to {@link #parseUnchecked(String, JavaType)} when invoked.
   *  
   *  @param <T> Object type.
   *  @param objectType The target object type.
   *  @return The parser {@link Function}.
   */
  public <T> Function<String, T> uncheckedParser(JavaType objectType) {
    return json -> parseUnchecked(json, objectType);
  }
  
  /**
   *  Outputs a given object to JSON form.
   *  
   *  @param object The object to output.
   *  @return The JSON {@link String} representation of the object.
   *  @throws JsonOutputException If an error occurs while formatting the object.
   */
  public String format(Object object) throws JsonOutputException {
    return format(object, JsonOutputException::new);
  }
  
  /**
   *  An unchecked variant of {@link #format(Object)}.
   *  
   *  @param object The object to output.
   *  @return The JSON {@link String} representation of the object.
   */
  public String formatUnchecked(Object object) {
    return format(object, RuntimeJsonException::new);
  }
  
  /**
   *  A variant of {@link #format(Object)} that throws a custom exception type upon
   *  encountering a formatting error.
   *  
   *  @param <X> Custom exception type.
   *  @param object The object to output.
   *  @param exceptionWrapper Maps a {@link JsonProcessingException} thrown during parsing to a custom exception type.
   *  @return The JSON {@link String} representation of the object.
   *  @throws X If an error occurs while formatting the object.
   */
  public <X extends Throwable> String format(Object object, 
                                             Function<? super JsonProcessingException, X> exceptionWrapper) throws X {
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw exceptionWrapper.apply(e);
    }
  }
  
  /**
   *  Constructs a complex {@link JavaType} token corresponding to a simple {@link Class} type.
   *  
   *  @param type The class type.
   *  @return A corresponding {@link JavaType} instance.
   */
  public static JavaType typeOf(Class<?> type) {
    return TypeFactory.defaultInstance().constructType(type);
  }
  
  /**
   *  Constructs a complex {@link JavaType} token corresponding to a {@link Class} type that
   *  has been parametrised with one or more {@link Class} parameter types.
   *  
   *  @param parametrized The encapsulating (container) class type.
   *  @param parameterClasses The parameter classes.
   *  @return A corresponding {@link JavaType} instance.
   */
  public static JavaType typeOf(Class<?> parametrized, Class<?>... parameterClasses) {
    return TypeFactory.defaultInstance().constructParametricType(parametrized, parameterClasses);
  }
}
