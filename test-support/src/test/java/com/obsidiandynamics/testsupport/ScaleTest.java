package com.obsidiandynamics.testsupport;

import static org.junit.Assert.*;

import org.junit.*;

public final class ScaleTest {
  @Test
  public void testUnity() {
    assertEquals(1, Scale.unity().get().magnitude());
  }
  
  @Test
  public void testMagnitude() {
    assertEquals(100, Scale.by(100).magnitude());
  }
}
