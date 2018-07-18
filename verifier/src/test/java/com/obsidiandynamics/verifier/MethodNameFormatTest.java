package com.obsidiandynamics.verifier;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class MethodNameFormatTest {
  @Test
  public void testPresets() {
    assertEquals("withField", MethodNameFormat.Presets.withXxx.getMethodName("field"));
    assertEquals("setField", MethodNameFormat.Presets.setXxx.getMethodName("field"));
    assertEquals("field", MethodNameFormat.Presets.xxx.getMethodName("field"));
  }
  
  @Test
  public void testPresetsConformance() {
    Assertions.assertUtilityClassWellDefined(MethodNameFormat.Presets.class);
  }
  
  @Test
  public void testAddPrefix() {
    assertEquals("withField", MethodNameFormat.addPrefix("with").getMethodName("field"));
  }
  
  @Test
  public void testStripSuffix() {
    assertEquals("field", MethodNameFormat.stripSuffix("Millis").getMethodName("field"));
    assertEquals("field", MethodNameFormat.stripSuffix("Millis").getMethodName("fieldMillis"));
  }
  
  @Test
  public void testThenChaining() {
    assertEquals("withField", 
                 MethodNameFormat
                 .stripSuffix("Millis")
                 .then(MethodNameFormat.addPrefix("with"))
                 .getMethodName("fieldMillis"));
  }
}
