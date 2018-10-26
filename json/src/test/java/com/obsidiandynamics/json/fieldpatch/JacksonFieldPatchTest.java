package com.obsidiandynamics.json.fieldpatch;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class JacksonFieldPatchTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(JacksonFieldPatch.class);
  }
  
  @Test
  public void testModule() {
    assertNotNull(JacksonFieldPatch.module());
  }
}
