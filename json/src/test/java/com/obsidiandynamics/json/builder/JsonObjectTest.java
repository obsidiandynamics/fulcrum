package com.obsidiandynamics.json.builder;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.json.*;

public final class JsonObjectTest {
  @Test
  public void testFormatEmpty() throws Exception {
    final String json = JsonObject.create().format(Json.getInstance());
    assertEquals("{}", json);
  }
  
  @Test
  public void testFormatFlat() throws Exception {
    final String json = JsonObject.create()
        .field("string", "value1")
        .field("boolean", false)
        .field("integer", 42)
        .field("decimal", 3.14)
        .field("null", null)
        .format(Json.getInstance());
    assertEquals("{\"string\":\"value1\",\"boolean\":false,\"integer\":42,\"decimal\":3.14}", json);
  }
  
  @Test
  public void testFormatNestedObject() throws Exception {
    final String json = JsonObject.create()
        .field("obj", JsonObject.create().field("foo", "bar"))
        .format(Json.getInstance());
    assertEquals("{\"obj\":{\"foo\":\"bar\"}}", json);
  }
  
  @Test
  public void testFormatNestedArray() throws Exception {
    final String json = JsonObject.create()
        .field("array", JsonArray.create().element("one").element("two").element(3).element(4.0))
        .formatUnchecked(Json.getInstance());
    assertEquals("{\"array\":[\"one\",\"two\",3,4.0]}", json);
  }
}
