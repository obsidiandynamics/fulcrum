package com.obsidiandynamics.flux;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class FluxSupportTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(FluxSupport.class);
  }
}
