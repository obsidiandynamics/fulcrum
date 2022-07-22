package com.obsidiandynamics.json.fieldpatch;

import java.io.*;

import org.junit.*;

import com.fasterxml.jackson.core.*;

public final class StandardFieldPatchDeserializerTest {
  @Test(expected=UnsupportedOperationException.class)
  public void test() throws IOException {
    new StandardFieldPatchDeserializer().deserialize(null, null);
  }
}
