package com.obsidiandynamics.json.builder;

import com.fasterxml.jackson.databind.*;
import com.obsidiandynamics.json.*;

/**
 *  Traits common to {@link JsonObject} and {@link JsonArray} that provide JSON
 *  serialization and JSON-to-object mapping behaviour.
 */
public interface JsonPart {
  /**
   *  Formats this object into its {@link String} representation using the supplied {@link Json}
   *  formatter.
   *  
   *  @param json The JSON formatter to use.
   *  @return The serialized {@link String} representation.
   *  @throws JsonOutputException If an error occurs.
   */
  default String format(Json json) throws JsonOutputException {
    return json.format(this);
  }
  
  /**
   *  An unchecked variant of {@link #format(Json)}.
   *  
   *  @param json The JSON formatter to use.
   *  @return The serialized {@link String} representation.
   */
  default String formatUnchecked(Json json) {
    return json.formatUnchecked(this);
  }
  
  /**
   *  Maps this JSON tree representation into a POJO, as though it was first
   *  serialized using {@link Json#format(Object)} and then parsed with {@link Json#parse(String, Class)}.
   *  
   *  @param <T> Object type.
   *  @param json The JSON parser/formatter to use.
   *  @param objectType The target object type.
   *  @return The parsed object.
   *  @throws JsonOutputException If an error occurred in the serialization step.
   *  @throws JsonInputException If an error occurred in the deserialization step.
   */
  default <T> T map(Json json, Class<T> objectType) throws JsonOutputException, JsonInputException {
    return json.parse(format(json), objectType);
  }
  
  /**
   *  Maps this JSON tree representation into a POJO, as though it was first
   *  serialized using {@link Json#format(Object)} and then parsed with {@link Json#parse(String, JavaType)}.
   *  
   *  @param <T> Object type.
   *  @param json The JSON parser/formatter to use.
   *  @param objectType The target object type.
   *  @return The parsed object.
   *  @throws JsonOutputException If an error occurred in the serialization step.
   *  @throws JsonInputException If an error occurred in the deserialization step.
   */
  default <T> T map(Json json, JavaType objectType) throws JsonOutputException, JsonInputException {
    return json.parse(format(json), objectType);
  }
  
  /**
   *  An unchecked variant of {@link #map(Json, Class)}.
   *  
   *  @param <T> Object type.
   *  @param json The JSON parser/formatter to use.
   *  @param objectType The target object type.
   *  @return The parsed object.
   */
  default <T> T mapUnchecked(Json json, Class<T> objectType) {
    return json.parseUnchecked(formatUnchecked(json), objectType);
  }
  
  /**
   *  An unchecked variant of {@link #map(Json, JavaType)}.
   *  
   *  @param <T> Object type.
   *  @param json The JSON parser/formatter to use.
   *  @param objectType The target object type.
   *  @return The parsed object.
   */
  default <T> T mapUnchecked(Json json, JavaType objectType) {
    return json.parseUnchecked(formatUnchecked(json), objectType);
  }
}
