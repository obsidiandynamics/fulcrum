package com.obsidiandynamics.func;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.*;

import org.junit.*;

public final class CheckedRunnableTest {
  @Test
  public void testRun() throws Exception {
    final CheckedRunnable<Exception> r = CheckedRunnable::nop;
    r.run();
  }
  
  @Test
  public void testWrap() {
    final AtomicBoolean ran = new AtomicBoolean();
    final CheckedRunnable<RuntimeException> r = CheckedRunnable.wrap(() -> ran.set(true));
    r.run();
    assertTrue(ran.get());
  }
}
