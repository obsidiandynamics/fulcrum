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
  public void testToChecked() {
    final AtomicBoolean ran = new AtomicBoolean();
    final CheckedRunnable<RuntimeException> r = CheckedRunnable.toChecked(() -> ran.set(true));
    r.run();
    assertTrue(ran.get());
  }

  @Test
  public void testToUnchecked() {
    final AtomicBoolean ran = new AtomicBoolean();
    final Runnable r = CheckedRunnable.toUnchecked(() -> ran.set(true));
    r.run();
    assertTrue(ran.get());
  }
}
