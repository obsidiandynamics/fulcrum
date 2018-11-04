package com.obsidiandynamics.json.builder;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import com.fasterxml.jackson.annotation.*;
import com.obsidiandynamics.json.*;

public final class JsonPartTest {
  @Test
  public void testMapObject() throws JsonOutputException, JsonInputException {
    final TestClass parsed = JsonObject.create()
        .field("str", "string")
        .field("num", 42)
        .map(Json.getInstance(), TestClass.class);
    
    assertEquals(new TestClass("string", 42), parsed);
  }
  
  @Test
  public void testMapUncheckedObject() {
    final TestClass parsed = JsonObject.create()
        .field("str", "string")
        .field("num", 42)
        .mapUnchecked(Json.getInstance(), TestClass.class);
    
    assertEquals(new TestClass("string", 42), parsed);
  }
  
  @Test
  public void testMapArray() throws JsonOutputException, JsonInputException {
    final Object parsed = JsonArray.create()
        .element(JsonObject.create()
                 .field("str", "string")
                 .field("num", 42))
        .element(JsonObject.create()
                 .field("str", "another")
                 .field("num", 3.14))
        .map(Json.getInstance(), Json.typeOf(List.class, TestClass.class));
    
    assertEquals(asList(new TestClass("string", 42), new TestClass("another", 3.14)), parsed);
  }
  
  @Test
  public void testMapUncheckedArray() {
    final Object parsed = JsonArray.create()
        .element(JsonObject.create()
                 .field("str", "string")
                 .field("num", 42))
        .element(JsonObject.create()
                 .field("str", "another")
                 .field("num", 3.14))
        .mapUnchecked(Json.getInstance(), Json.typeOf(List.class, TestClass.class));
    
    assertEquals(asList(new TestClass("string", 42), new TestClass("another", 3.14)), parsed);
  }
  
  static final class TestClass {
    final String str;
    
    final Number num;

    public TestClass(@JsonProperty("str") String str,
                     @JsonProperty("num") Number num) {
      this.str = str;
      this.num = num;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((num == null) ? 0 : num.hashCode());
      result = prime * result + ((str == null) ? 0 : str.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      TestClass other = (TestClass) obj;
      if (num == null) {
        if (other.num != null)
          return false;
      } else if (!num.equals(other.num))
        return false;
      if (str == null) {
        if (other.str != null)
          return false;
      } else if (!str.equals(other.str))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return TestClass.class.getSimpleName() + " [str=" + str + ", num=" + num + "]";
    }
  }
}
