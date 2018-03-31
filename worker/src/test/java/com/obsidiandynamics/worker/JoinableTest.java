package com.obsidiandynamics.worker;

import static org.junit.Assert.*;

import org.junit.*;

public final class JoinableTest {
  private static final class TestJoinable implements Joinable {
    private final boolean join;
    
    TestJoinable(boolean join) {
      this.join = join;
    }

    @Override
    public boolean join(long timeoutMillis) throws InterruptedException {
      Thread.sleep(10);
      return join;
    }
  }
  
  @Test
  public void testJoinAllEmptyPass() throws InterruptedException {
    assertTrue(Joinable.joinAll(1_000));
  }
  
  @Test
  public void testJoinAllPass() throws InterruptedException {
    final Joinable j = new TestJoinable(true);
    assertTrue(Joinable.joinAll(1_000, j));
  }
  
  @Test
  public void testJoinAllPassNoTimeout() throws InterruptedException {
    final Joinable j = new TestJoinable(true);
    assertTrue(Joinable.joinAll(0, j));
  }
  
  @Test
  public void testJoinAllFail() throws InterruptedException {
    final Joinable j = new TestJoinable(false);
    assertFalse(Joinable.joinAll(1_000, j));
  }
  
  @Test
  public void testJoinAllOutOfTime() throws InterruptedException {
    final Joinable j0 = new TestJoinable(true);
    final Joinable j1 = new TestJoinable(false);
    assertFalse(Joinable.joinAll(1, j0, j1));
  }
  
  @Test
  public void testNop() throws InterruptedException {
    assertTrue(Joinable.nop().join(0));
  }
}
