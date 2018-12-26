package com.obsidiandynamics.worker;

import static junit.framework.TestCase.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import com.obsidiandynamics.assertion.*;
import com.obsidiandynamics.await.*;
import com.obsidiandynamics.junit.*;
import com.obsidiandynamics.threads.*;

import nl.jqno.equalsverifier.*;

@RunWith(Parameterized.class)
public final class WorkerThreadTest {
  @Parameterized.Parameters
  public static List<Object[]> data() {
    return TestCycle.timesQuietly(1);
  }

  private final Timesert wait = Timesert.wait(10_000);

  @Test
  public void testSingleRun() {
    final AtomicInteger counter = new AtomicInteger();
    final WorkerThread thread = WorkerThread.builder()
        .onCycle(t -> {
          counter.incrementAndGet();
          assertEquals(WorkerState.RUNNING, t.getState());
          t.terminate();
          assertEquals(WorkerState.TERMINATING, t.getState());
          t.terminate(); // second call should have no effect
          assertEquals(WorkerState.TERMINATING, t.getState());
        })
        .build();
    assertEquals(WorkerState.CONCEIVED, thread.getState());
    thread.start();

    final boolean joined = thread.joinSilently(60_000);
    assertTrue(joined);
    assertEquals(1, counter.get());
    Threads.sleep(10);
    assertEquals(1, counter.get());
    assertFalse(thread.getDriverThread().isAlive());
    assertEquals(WorkerState.TERMINATED, thread.getState());
  }

  @Test
  public void testTerminateOnInterrupt() {
    final AtomicInteger counter = new AtomicInteger();
    final WorkerShutdown onShutdown = mock(WorkerShutdown.class);
    final WorkerThread thread = WorkerThread.builder()
        .onCycle(t -> {
          counter.incrementAndGet();
          throw new InterruptedException();
        })
        .onShutdown(onShutdown)
        .buildAndStart();
    thread.joinSilently();

    assertEquals(WorkerState.TERMINATED, thread.getState());
    assertEquals(1, counter.get());
    assertFalse(thread.getDriverThread().isAlive());
    verify(onShutdown).handle(eq(thread), any(InterruptedException.class));
  }

  @Test
  public void testTerminateOnUnhandledException() {
    final WorkerShutdown onShutdown = mock(WorkerShutdown.class);
    final RuntimeException exception = new RuntimeException("Boom");
    final WorkerThread thread = WorkerThread.builder()
        .onCycle(t -> {
          throw exception;
        })
        .onShutdown(onShutdown)
        .onUncaughtException((t, x) -> {})
        .build();
    thread.start();
    thread.joinSilently();

    assertEquals(WorkerState.TERMINATED, thread.getState());
    assertFalse(thread.getDriverThread().isAlive());
    verify(onShutdown).handle(eq(thread), eq(exception));
  }

  @Test
  public void testLifecycleEvents() {
    final WorkerStartup onStartup = mock(WorkerStartup.class);
    final WorkerShutdown onShutdown = mock(WorkerShutdown.class);
    final WorkerThread thread = WorkerThread.builder()
        .onCycle(t -> Thread.sleep(10))
        .onStartup(onStartup)
        .onShutdown(onShutdown)
        .build();
    thread.start();

    wait.until(() -> {
      verify(onStartup).handle(eq(thread));
    });
    thread.terminate().joinSilently();
    verify(onShutdown).handle(eq(thread), any());
  }

  @Test(expected=IllegalStateException.class)
  public void testTerminateOnConceive() {
    final WorkerThread thread = WorkerThread.builder()
        .onCycle(t -> {})
        .build();
    thread.terminate();
    assertEquals(WorkerState.TERMINATED, thread.getState());
    thread.start();
  }

  @Test(expected=IllegalStateException.class)
  public void testBuildWithWorker() {
    WorkerThread.builder().build();
  }

  @Test(expected=IllegalStateException.class)
  public void testStartTwice() {
    final WorkerThread thread = WorkerThread.builder()
        .onCycle(t -> t.terminate())
        .build();
    thread.start();
    thread.start(); // this call should throw an exception
  }

  @Test
  public void testToString() {
    final WorkerThread thread = WorkerThread.builder()
        .onCycle(t -> {})
        .build();
    Assertions.assertToStringOverride(thread);
  }

  @Test
  public void testJoinInterrupted() {
    final WorkerThread thread = WorkerThread.builder()
        .onCycle(t -> Thread.sleep(10))
        .build();
    thread.start();
    Thread.currentThread().interrupt();
    thread.joinSilently();
    assertTrue(Thread.interrupted());

    thread.terminate().joinSilently();
  }

  @Test
  public void testWhileNotInterrupted() {
    final WorkerThread thread = WorkerThread.builder()
        .onCycle(t -> Thread.sleep(10))
        .buildAndStart();

    final Runnable r = mock(Runnable.class);
    new Thread(() -> {
      Timesert.wait(1_000).until(() -> {
        verify(r, atLeastOnce()).run();
      });
      thread.terminate();
    }, "terminator-thread").start();

    thread.whileNotInterrupted(r);
    thread.joinSilently();
    assertFalse(Thread.interrupted());
  }

  @Test
  public void testJoinTimeout() throws InterruptedException {
    final WorkerThread thread = WorkerThread.builder()
        .onCycle(t -> Thread.sleep(10))
        .build();
    thread.start();
    final boolean joined = thread.join(10);
    assertFalse(joined);

    thread.terminate().join();
  }

  @Test
  public void testOptions() {
    final WorkerOptions options = new WorkerOptions()
        .withName("TestThread")
        .withDaemon(true)
        .withPriority(3);
    final WorkerThread thread = WorkerThread.builder()
        .withOptions(options)
        .onCycle(t -> {})
        .build();
    assertEquals(options.getName(), thread.getName());
    assertEquals(options.isDaemon(), thread.isDaemon());
    assertEquals(options.getPriority(), thread.getPriority());
    Assertions.assertToStringOverride(options);
  }

  @Test
  public void testEqualsHashCode() {
    EqualsVerifier.forClass(WorkerThread.class)
    .withPrefabValues(Thread.class, new Thread("red"), new Thread("black"))
    .withOnlyTheseFields("driver")
    .verify();
  }

  private static class ListExceptionHandler implements WorkerExceptionHandler {
    private final List<Throwable> exceptions = new CopyOnWriteArrayList<>();

    @Override
    public void handle(WorkerThread thread, Throwable exception) {
      exceptions.add(exception);
    }
  }

  @Test
  public void testUncaughtExceptionHandlerOnStartup() {
    final RuntimeException causeOnStartup = new RuntimeException();
    final ListExceptionHandler handler = new ListExceptionHandler();
    final WorkerThread thread = WorkerThread.builder()
        .onStartup(t -> {
          throw causeOnStartup;
        })
        .onCycle(t -> {})
        .onUncaughtException(handler)
        .build();

    thread.start();
    thread.joinSilently();

    assertEquals(1, handler.exceptions.size());
    assertEquals(causeOnStartup, handler.exceptions.get(0));
  }

  @Test
  public void testUncaughtExceptionHandlerOnCycle() {
    final RuntimeException causeOnCycle = new RuntimeException();
    final ListExceptionHandler handler = new ListExceptionHandler();
    final WorkerThread thread = WorkerThread.builder()
        .onCycle(t -> {
          throw causeOnCycle;
        })
        .onUncaughtException(handler)
        .build();

    thread.start();
    thread.joinSilently();

    assertEquals(1, handler.exceptions.size());
    assertEquals(causeOnCycle, handler.exceptions.get(0));
  }

  @Test
  public void testUncaughtExceptionHandlerOnShutdown() {
    final RuntimeException causeOnCycle = new RuntimeException();
    final RuntimeException causeOnShutdown = new RuntimeException();
    final ListExceptionHandler handler = new ListExceptionHandler();
    final WorkerThread thread = WorkerThread.builder()
        .onCycle(t -> {
          throw causeOnCycle;
        })
        .onShutdown((t, x) -> {
          throw causeOnShutdown;
        })
        .onUncaughtException(handler)
        .build();

    thread.start();
    thread.joinSilently();

    assertEquals(2, handler.exceptions.size());
    assertEquals(causeOnCycle, handler.exceptions.get(0));
    assertEquals(causeOnShutdown, handler.exceptions.get(1));
  }

  @Test
  public void testExceptionInUncaughtExceptionHandler() {
    final RuntimeException causeOnCycle = new RuntimeException();
    final RuntimeException causeOnUncaughtException = new RuntimeException();
    final ListExceptionHandler handler = new ListExceptionHandler();
    final WorkerThread thread = WorkerThread.builder()
        .onCycle(t -> {
          throw causeOnCycle;
        })
        .onUncaughtException((t, x) -> {
          handler.handle(t, x);
          throw causeOnUncaughtException;
        })
        .build();

    final AtomicReference<Throwable> driverExceptionHandler = new AtomicReference<>();
    thread.getDriverThread().setUncaughtExceptionHandler((t, x) -> driverExceptionHandler.set(x));
    thread.start();
    thread.joinSilently();

    assertEquals(1, handler.exceptions.size());
    assertEquals(causeOnCycle, handler.exceptions.get(0));
    assertEquals(causeOnUncaughtException, driverExceptionHandler.get());
  }

  @Test
  public void testPrintStreamUncaughtExceptionHandler() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final PrintStream p = new PrintStream(baos);
    final WorkerThread thread = WorkerThread.builder()
        .onCycle(t -> {
          throw new RuntimeException("boom");
        })
        .onUncaughtException(WorkerExceptionHandler.forPrintStream(p))
        .build();

    thread.start();
    thread.joinSilently();

    assertTrue(baos.toByteArray().length > 0);
  }

  @Test
  public void testWithNameNoFrags() {
    final WorkerOptions opts = new WorkerOptions()
        .withName(WorkerThreadTest.class);
    assertEquals(WorkerThreadTest.class.getSimpleName(), opts.getName());
  }

  @Test
  public void testWithNameFrags() {
    final WorkerOptions opts = new WorkerOptions()
        .withName(WorkerThreadTest.class, 1, 2, 3);
    assertEquals(WorkerThreadTest.class.getSimpleName() + "-1-2-3", opts.getName());
  }

  @Test
  public void testDaemon() {
    final WorkerOptions opts = new WorkerOptions().daemon();
    assertTrue(opts.isDaemon());
  }
}
