package com.obsidiandynamics.json.fieldpatch;

import java.io.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.deser.std.*;
import com.obsidiandynamics.func.*;
import com.obsidiandynamics.json.fieldpatch.FieldPatch.*;

public final class StandardFieldPatchDeserializer extends StdDeserializer<StandardFieldPatch<?>> implements ContextualDeserializer {
  private static final long serialVersionUID = 1L;

  StandardFieldPatchDeserializer() {
    super(Classes.<Class<StandardFieldPatch<?>>>cast(StandardFieldPatch.class));
  }

  @Override
  public StandardFieldPatch<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
    final Class<?> containedType = property.getType().containedTypeOrUnknown(0).getRawClass();
    return new TypedFieldPatchDeserializer(containedType);
  }

  private static class TypedFieldPatchDeserializer extends StdDeserializer<FieldPatch<?>> {
    private static final long serialVersionUID = 1L;

    private final Class<?> valueType;

    TypedFieldPatchDeserializer(Class<?> valueType) {
      super(Classes.<Class<FieldPatch<?>>>cast(FieldPatch.class));
      this.valueType = valueType;
    }

    @Override
    public FieldPatch<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      final TreeNode node = p.readValueAsTree();
      final TreeNode setNode = node.get("value");
      final Object value = setNode != null ? p.getCodec().treeToValue(setNode, valueType) : null;
      return FieldPatch.nullableOf(value);
    }
  }
}
