package com.obsidiandynamics.json.fieldpatch;

import com.fasterxml.jackson.databind.module.*;
import com.obsidiandynamics.func.*;

public final class JacksonFieldPatch {
  private static final SimpleModule module = new SimpleModule();
  
  static {
    registerSerializers(module);
  }
  
  public static void registerSerializers(SimpleModule module) {
    module
    .addSerializer(new StandardFieldPatchSerializer())
    .addDeserializer(Classes.cast(FieldPatch.class), new StandardFieldPatchDeserializer());
  }
  
  public static com.fasterxml.jackson.databind.Module module() {
    return module;
  }
  
  private JacksonFieldPatch() {}
}
