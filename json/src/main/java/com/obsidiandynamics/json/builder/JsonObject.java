package com.obsidiandynamics.json.builder;

import java.util.*;

/**
 *  A structured representation of a JSON object.
 */
public interface JsonObject extends JsonPart {
  JsonObject field(String name, Object value);
  
  final class JsonObjectImpl extends LinkedHashMap<String, Object> implements JsonObject {
    private static final long serialVersionUID = 1L;

    @Override
    public JsonObject field(String name, Object value) {
      put(name, value);
      return this;
    }
  }
  
  /**
   *  Creates a new {@link JsonObject}, backed by a {@link LinkedHashMap}.
   *  
   *  @return A new {@link JsonObject}.
   */
  static JsonObject create() {
    return new JsonObjectImpl();
  }
}
