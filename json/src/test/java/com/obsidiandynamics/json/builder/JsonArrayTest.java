package com.obsidiandynamics.json.builder;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.json.*;

public final class JsonArrayTest {
  @Test
  public void testFormatEmpty() throws Exception {
    final String json = JsonArray.create().format(Json.getInstance());
    assertEquals("[]", json);
  }

  @Test
  public void testFormatFlat() throws Exception {
    final String json = JsonArray.create()
        .element("one")
        .element("two")
        .element(3)
        .element(4.0)
        .element(false)
        .element(null)
        .format(Json.getInstance());
    assertEquals("[\"one\",\"two\",3,4.0,false,null]", json);
  }

  @Test
  public void testFormatNestedArray() throws Exception {
    final String json = JsonArray.create()
        .element(JsonArray.create()
                 .element("foo")
                 .element("bar"))
        .format(Json.getInstance());
    assertEquals("[[\"foo\",\"bar\"]]", json);
  }

  @Test
  public void testFormatNestedObject() {
    final String json = JsonArray.create()
        .element(JsonObject.create()
                 .field("foo", "bar"))
        .formatUnchecked(Json.getInstance());
    assertEquals("[{\"foo\":\"bar\"}]", json);
  }
}
