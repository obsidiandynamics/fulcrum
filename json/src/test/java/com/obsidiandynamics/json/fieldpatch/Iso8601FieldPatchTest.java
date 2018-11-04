package com.obsidiandynamics.json.fieldpatch;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.junit.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.obsidiandynamics.assertion.*;

import nl.jqno.equalsverifier.*;

public final class Iso8601FieldPatchTest {
  private static ObjectMapper createMapper() {
    return new ObjectMapper().registerModule(JacksonFieldPatch.module());
  }
  
  static final class TestRoot {
    @JsonProperty
    Iso8601FieldPatch patch;
  }

  @Test
  public void testRoundTrip() throws IOException {
    final ObjectMapper mapper = createMapper();
    final TestRoot root = new TestRoot();
    final Date epoch = new Date(0);
    root.patch = Iso8601FieldPatch.of(epoch);
    
    final String json = mapper.writeValueAsString(root);
    assertEquals("{\"patch\":{\"value\":\"1970-01-01T00:00:00.000Z\"}}", json);
    final TestRoot decoded = mapper.readValue(json, TestRoot.class);
    assertNotNull(decoded.patch);
    assertEquals(epoch, decoded.patch.get());
    Assertions.assertToStringOverride(decoded.patch);
  }

  @Test
  public void testDeserializeWithEmptyJsonObject() throws IOException {
    final ObjectMapper mapper = createMapper();
    final String json = "{\"patch\":{}}";
    final TestRoot decoded = mapper.readValue(json, TestRoot.class);
    assertNotNull(decoded.patch);
    assertNull(decoded.patch.get());
  }
  
  @Test
  public void testEqualsHashCode() {
    EqualsVerifier.forClass(Iso8601FieldPatch.class).verify();
  }
}
