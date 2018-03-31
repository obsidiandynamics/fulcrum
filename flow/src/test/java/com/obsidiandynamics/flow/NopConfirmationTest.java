package com.obsidiandynamics.flow;

import org.junit.*;

public final class NopConfirmationTest {
  @Test
  public void test() {
    final NopConfirmation c = NopConfirmation.getInstance();
    c.confirm();
  }
}
