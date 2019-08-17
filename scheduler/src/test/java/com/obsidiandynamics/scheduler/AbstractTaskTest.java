package com.obsidiandynamics.scheduler;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class AbstractTaskTest {
  private static class TestTask extends AbstractTask<String> {
    TestTask(long time, String id) {
      super(time, id);
    }

    @Override
    public void execute(TaskScheduler scheduler) {
      assertNotNull(scheduler);
    }
  }
  
  @Test
  public void test() {
    final TestTask t = new TestTask(100, "test");
    assertEquals(100, t.getTime());
    assertEquals("test", t.getId());
    
    Assertions.assertToStringOverride(t);
  }
}
