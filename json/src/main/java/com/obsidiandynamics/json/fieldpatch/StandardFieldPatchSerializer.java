package com.obsidiandynamics.json.fieldpatch;

import java.io.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.std.*;
import com.obsidiandynamics.func.*;
import com.obsidiandynamics.json.fieldpatch.FieldPatch.*;

public final class StandardFieldPatchSerializer extends StdSerializer<StandardFieldPatch<?>> {
  private static final long serialVersionUID = 1L;
  
  StandardFieldPatchSerializer() {
    super(Classes.<Class<StandardFieldPatch<?>>>cast(StandardFieldPatch.class));
  }

  @Override
  public void serialize(StandardFieldPatch<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeStartObject();
    gen.writeFieldName("value");
    gen.writeObject(value.get());
    gen.writeEndObject();
  }
}
