package com.obsidiandynamics.dyno;

import org.junit.*;

public final class BenchmarkTargetTest {
  @Test
  public void testDefaultMethodsCoverage() throws Exception {
    final BenchmarkTarget target = __ -> {};
    target.setup();
    target.tearDown();
  }
}
