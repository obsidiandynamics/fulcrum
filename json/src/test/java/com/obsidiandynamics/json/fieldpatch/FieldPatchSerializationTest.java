package com.obsidiandynamics.json.fieldpatch;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.junit.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;

public final class FieldPatchSerializationTest {
  private static class TestValue {
    @JsonProperty
    private final String foo;

    TestValue(@JsonProperty("foo") String foo) { this.foo = foo; }

    @Override
    public int hashCode() {
      return Objects.hashCode(foo);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof TestValue) {
        final TestValue that = (TestValue) obj;
        return Objects.equals(foo, that.foo);
      } else {
        return false;
      }
    }
  }

  private static ObjectMapper createMapper() {
    return new ObjectMapper().registerModule(JacksonFieldPatch.module());
  }

  static final class AttributeRoot {
    @JsonProperty
    private FieldPatch<TestValue> patch;
  }

  @Test
  public void testRoundTripAttribute() throws IOException {
    final ObjectMapper mapper = createMapper();
    final AttributeRoot root = new AttributeRoot();
    root.patch = FieldPatch.of(new TestValue("bar"));

    final String json = mapper.writeValueAsString(root);
    assertEquals("{\"patch\":{\"value\":{\"foo\":\"bar\"}}}", json);

    final AttributeRoot decoded = mapper.readValue(json, AttributeRoot.class);
    assertEquals(new TestValue("bar"), decoded.patch.get());
  }

  @Test
  public void testRoundTripAttributeNullValue() throws IOException {
    final ObjectMapper mapper = createMapper();
    final AttributeRoot root = new AttributeRoot();
    root.patch = FieldPatch.ofNull();

    final String json = mapper.writeValueAsString(root);
    assertEquals("{\"patch\":{\"value\":null}}", json);

    final AttributeRoot decoded = mapper.readValue(json, AttributeRoot.class);
    assertNull(decoded.patch.get());
  }

  @Test
  public void testRoundTripAttributeNullPatch() throws IOException {
    final ObjectMapper mapper = createMapper();
    final AttributeRoot root = new AttributeRoot();

    final String json = mapper.writeValueAsString(root);
    assertEquals("{\"patch\":null}", json);

    final AttributeRoot decoded = mapper.readValue(json, AttributeRoot.class);
    assertNull(decoded.patch);
  }

  static final class AttributeRootWithMissingType {
    @JsonProperty
    private FieldPatch<TestValue> patch;
  }

  static final class ConstructorRoot {
    @JsonProperty
    private final FieldPatch<TestValue> patch;

    public ConstructorRoot(@JsonProperty("patch") FieldPatch<TestValue> patch) { 
      this.patch = patch;
    }
  }

  @Test
  public void testRoundTripConstructor() throws IOException {
    final ObjectMapper mapper = createMapper();
    final ConstructorRoot root = new ConstructorRoot(FieldPatch.of(new TestValue("bar")));

    final String json = mapper.writeValueAsString(root);
    assertEquals("{\"patch\":{\"value\":{\"foo\":\"bar\"}}}", json);

    final ConstructorRoot decoded = mapper.readValue(json, ConstructorRoot.class);
    assertEquals(new TestValue("bar"), decoded.patch.get());
  }

  static final class MethodsRoot {
    @JsonIgnore
    private FieldPatch<TestValue> patch;

    @JsonProperty
    public FieldPatch<TestValue> getPatch() {
      return patch;
    }

    @JsonProperty
    public void setPatch(FieldPatch<TestValue> patch) {
      this.patch = patch;
    }
  }

  @Test
  public void testRoundTripMethods() throws IOException {
    final ObjectMapper mapper = createMapper();
    final MethodsRoot root = new MethodsRoot();
    root.setPatch(FieldPatch.of(new TestValue("bar")));

    final String json = mapper.writeValueAsString(root);
    assertEquals("{\"patch\":{\"value\":{\"foo\":\"bar\"}}}", json);

    final MethodsRoot decoded = mapper.readValue(json, MethodsRoot.class);
    assertEquals(new TestValue("bar"), decoded.getPatch().get());
  }
  
  static final class CustomPatch implements FieldPatch<TestValue> {
    @JsonProperty
    private TestValue customValue;
    
    @Override
    public TestValue get() {
      return customValue;
    }
  }
  
  static final class CustomRoot {
    @JsonProperty
    private CustomPatch patch;
  }

  @Test
  public void testRoundWithCustomPatch() throws IOException {
    final ObjectMapper mapper = createMapper();
    final CustomRoot root = new CustomRoot();
    root.patch = new CustomPatch();
    root.patch.customValue = new TestValue("bar");

    final String json = mapper.writeValueAsString(root);
    assertEquals("{\"patch\":{\"customValue\":{\"foo\":\"bar\"}}}", json);

    final CustomRoot decoded = mapper.readValue(json, CustomRoot.class);
    assertEquals(new TestValue("bar"), decoded.patch.get());
  }
}
