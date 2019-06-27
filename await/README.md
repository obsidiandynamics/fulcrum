<img src="https://raw.githubusercontent.com/wiki/obsidiandynamics/fulcrum/images/fulcrum-logo.png" width="90px" alt="logo"/> `fulcrum-await`
===
A µ-library for awaiting asynchronous actions.

# What is it
Await comprises two classes — `Await` and `Timesert`.

## `Await`
Building block for awaiting a specific condition. The utility methods block the calling thread until a certain condition, described by the specified `BooleanSupplier` evaluates to `true`.

There are variations of the blocking methods - some return a `boolean`, indicating whether the condition has been satisfied within the allotted time frame, while others throw a `TimeoutException`. You can specify an upper bound on how long to wait for, as well as the checking interval (which otherwise defaults to 1 ms). All times are in milliseconds.

## `Timesert`
Timesert (a portmanteau of _time_ and _assert_) adds timed assertion testing to `Await`, and is probably the real reason why you are here. Timesert allows you to write efficient, reproducible assertions without resorting to `Thread.sleep()`.

Unlike Awaitility, this implementation is robust in the face of a system clock that doesn't satisfy the monotonic non-decreasing assumption. (While rare, this assumption may be violated when using NTP, and is particularly problematic on macOS.)

Timesert is useful when writing and testing network applications and asynchronous systems in general, as events don't happen instantly. For example, when sending a message you might want to assert that it has been received. But naively running an assertion on the receiver immediately following a send in an asynchronous environment will likely fail. Using Timesert allows for assertions to fail up silently to a certain point in time, after which the `AssertionError` is percolated to the caller and the test case fails.

# Getting started
A simple test that waits for a background task:
```java
// an asynchronous task that runs in the background
MyAsyncTask asyncTask = new MyAsyncTask();

// kick it off... may take some time to complete
asyncTask.start();

// allow up to 10 s, asserting every 5 ms
Timesert.wait(10_000).withIntervalMillis(5).until(() -> {
  assertTrue(asyncTask.isDone());
});
```

The background task in this example is just a plain old thread, but could be anything:
```java
class MyAsyncTask extends Thread {
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
```