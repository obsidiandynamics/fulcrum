package com.obsidiandynamics.assertion;

import static org.junit.Assert.*;

import org.junit.*;

public final class AssertionsTest {
  @Test
  public void testEnabled() {
    assertTrue(Assertions.areEnabled());
  }
  
  @Test
  public void testAssertRunnableTrue() {
    assertTrue(Assertions.assertRunnable(() -> {}));
  }
  
  @Test
  public void testSelfConformance() throws Exception {
    Assertions.assertUtilityClassWellDefined(Assertions.class);
  }
}
