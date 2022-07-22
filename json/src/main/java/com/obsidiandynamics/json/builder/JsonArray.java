package com.obsidiandynamics.json.builder;

import java.util.*;

/**
 *  A structured representation of a JSON array.
 */
public interface JsonArray extends JsonPart {
  JsonArray element(Object value);
  
  final class JsonArrayImpl extends ArrayList<Object> implements JsonArray {
    private static final long serialVersionUID = 1L;

    @Override
    public JsonArray element(Object value) {
      add(value);
      return this;
    }
  }
  
  /**
   *  Creates a new {@link JsonArray}, backed by an {@link ArrayList}.
   *  
   *  @return A new {@link JsonArray}.
   */
  static JsonArray create() {
    return new JsonArrayImpl();
  }
}
