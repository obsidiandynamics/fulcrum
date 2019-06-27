package com.obsidiandynamics.await;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.threads.*;

/**
 *  Sample test case that shows how {@link Timesert} should be used.
 */
public final class TimesertSampleTest {
  @Test
  public void testSample() {
    // an asynchronous task that runs in the background
    MyAsyncTask asyncTask = new MyAsyncTask();
    
    // kick it off... may take some time to complete
    asyncTask.start();
    
    // allow up to 10 s, asserting every 5 ms
    Timesert.wait(10_000).withIntervalMillis(5).until(() -> {
      assertTrue(asyncTask.isDone());
    });
  }
  
  private static class MyAsyncTask extends Thread {
    private volatile boolean done;
    
    boolean isDone() {
      return done;
    }
    
    @Override
    public void run() {
      Threads.sleep((long) (Math.random() * 10));
      done = true;
    }
  }
}
