package com.obsidiandynamics.await;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.*;

import org.junit.Test;

import junit.framework.*;

public final class TimesertTest {
  @Test
  public void testPass() {
    Timesert.wait(10).scale(2).withIntervalMillis(1).until(() -> {});
  }
  
  @Test
  public void testPassBoolean() {
    Timesert.wait(10).scale(2).withIntervalMillis(1).untilTrue(() -> true);
  }
  
  @Test
  public void testFail() {
    final String message = "Boom";
    try {
      Timesert.wait(20).withIntervalMillis(1).until(() -> { throw new AssertionError(message); });
      TestCase.fail("AssertionError not thrown");
    } catch (AssertionError e) {
      TestCase.assertEquals(message, e.getMessage());
    }
  }
  
  @Test
  public void testFailBoolean() {
    try {
      Timesert.wait(20).withIntervalMillis(1).untilTrue(() -> false);
    } catch (AssertionError e) {
      return;
    }
    TestCase.fail("AssertionError not thrown");
  }
  
  @Test
  public void testPartialFail() {
    final AtomicInteger calls = new AtomicInteger();
    Timesert.wait(0).withIntervalMillis(1).until(() -> { 
      if (calls.getAndIncrement() == 0) {
        try {
          Thread.sleep(1);
        } catch (InterruptedException ignored) {}
        throw new AssertionError("Boom"); 
      }
    });
  }
  
  @Test
  public void testInterrupted() {
    try {
      Timesert.wait(20).untilTrue(() -> {
        Thread.currentThread().interrupt();
        return false;
      });
    } catch (AssertionError ae) {
      fail("Unexpeced exception");
    } finally {
      TestCase.assertTrue(Thread.interrupted());
    }
  }
}
