package com.obsidiandynamics.verifier;

import org.junit.*;

public final class ConstructorArgsTest {
  @Test
  public void testPojo() {
    PojoVerifier.forClass(ConstructorArgs.class).excludeAccessors().verify();
  }
}
