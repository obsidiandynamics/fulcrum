package com.obsidiandynamics.nanoclock;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class NanoClockTest {
  @Test
  public void testConformance() throws Exception {
    Assertions.assertUtilityClassWellDefined(NanoClock.class);
  }

  @Test
  public void testTime() {
    final long beforeMillis = System.currentTimeMillis();
    final long nanoNow = NanoClock.now();
    final long afterMillis = System.currentTimeMillis();
    final long tolerance = 5_000;
    assertTrue(nanoNow > (beforeMillis - tolerance) * 1_000_000L);
    assertTrue(nanoNow < (afterMillis + tolerance) * 1_000_000L);
  }
}
