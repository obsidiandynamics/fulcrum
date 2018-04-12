package com.obsidiandynamics.shell;

import org.junit.*;

public final class SinkTest {
  @Test
  public void testNopCoverage() {
    Sink.nop().accept("anything");
  }
}
