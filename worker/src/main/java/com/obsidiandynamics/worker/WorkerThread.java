package com.obsidiandynamics.worker;

import java.util.*;
import java.util.concurrent.atomic.*;

public final class WorkerThread implements Terminable, Joinable {
  private final Thread driver;
  
  private final WorkerCycle worker;
  
  private final WorkerStartup onStartup;
  
  private final WorkerShutdown onShutdown;
  
  private final WorkerExceptionHandler onUncaughtException;
  
  private volatile WorkerState state = WorkerState.CONCEIVED;
  
  /** Indicates that the shutdown handler is about to be run. */
  private final AtomicBoolean shutdown = new AtomicBoolean();
  
  /** Indicates that the driver has been interrupted by {@link #shutdown()}. */
  private volatile boolean interrupted;
  
  /** Guards the changing of the thread state. */
  private final Object stateLock = new Object();
  
  WorkerThread(WorkerOptions options, 
               WorkerCycle onCycle, 
               WorkerStartup onStartup, 
               WorkerShutdown onShutdown, 
               WorkerExceptionHandler onUncaughtException) {
    this.worker = onCycle;
    this.onStartup = onStartup;
    this.onShutdown = onShutdown;
    this.onUncaughtException = onUncaughtException;
    driver = new Thread(this::run);
    
    if (options.getName() != null) {
      driver.setName(options.getName());
    }
    
    driver.setDaemon(options.isDaemon());
    driver.setPriority(options.getPriority());
  }
  
  public Thread getDriverThread() {
    return driver;
  }
  
  /**
   *  Starts the worker thread.
   */
  public final void start() {
    synchronized (stateLock) {
      if (state == WorkerState.CONCEIVED) {
        state = WorkerState.RUNNING;
        driver.start();
      } else {
        throw new IllegalStateException("Cannot start worker in state " + state);
      }
    }
  }
  
  /**
   *  Terminates the worker thread.
   *  
   *  @return A {@link Joinable} for the caller to wait on.
   */
  @Override
  public final Joinable terminate() {
    synchronized (stateLock) {
      if (state == WorkerState.CONCEIVED) {
        state = WorkerState.TERMINATED;
      } else if (state == WorkerState.RUNNING) {
        state = WorkerState.TERMINATING;
      }
    }
    
    // only interrupt the driver if it hasn't finished cycling, and at most once
    if (shutdown.compareAndSet(false, true)) {
      driver.interrupt();
      interrupted = true;
    }
    
    return this;
  }
  
  private void run() {
    Throwable exception = null;
    try {
      onStartup.handle(this);
      while (state == WorkerState.RUNNING) {
        cycle();
      }
    } catch (Throwable e) {
      synchronized (stateLock) {
        state = WorkerState.TERMINATING;
      }
      exception = e;
    } finally {
      try {
        handleUncaughtException(exception);
      } finally {
        if (shutdown.compareAndSet(false, true)) {
          // indicate that we've finished cycling - this way we won't get interrupted and 
          // can call the shutdown hook safely
        } else {
          // we will imminently get interrupted  wait before proceeding with the shutdown hook
          whileNotInterrupted(Thread::yield);
          Thread.interrupted(); // clear the interrupt before invoking the shutdown hook
        }
        
        try {
          onShutdown.handle(this, exception);
        } catch (Throwable e) {
          handleUncaughtException(e);
        } finally {
          synchronized (stateLock) {
            state = WorkerState.TERMINATED;
          }
        }
      }
    }
  }
  
  void whileNotInterrupted(Runnable r) {
    while (! interrupted) r.run();
  }
  
  private void handleUncaughtException(Throwable exception) {
    if (exception != null && ! (exception instanceof InterruptedException)) {
      onUncaughtException.handle(this, exception);
    }
  }
  
  /**
   *  Obtains the current state of the worker thread.
   *  
   *  @return The thread's state.
   */
  public final WorkerState getState() {
    return state;
  }
  
  private void cycle() throws InterruptedException {
    worker.cycle(this);
  }
  
  @Override
  public final boolean join(long timeoutMillis) throws InterruptedException {
    driver.join(timeoutMillis);
    return ! driver.isAlive();
  }
  
  public final String getName() {
    return driver.getName();
  }
  
  public final boolean isDaemon() {
    return driver.isDaemon();
  }
  
  public final int getPriority() {
    return driver.getPriority();
  }
  
  @Override
  public final int hashCode() {
    return Objects.hashCode(driver);
  }

  @Override
  public final boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof WorkerThread) {
      final WorkerThread that = (WorkerThread) obj;
      return Objects.equals(driver, that.driver);
    } else {
      return false;
    }
  }

  @Override
  public final String toString() {
    return WorkerThread.class.getSimpleName() + " [thread=" + driver + ", state=" + state + "]";
  }
  
  public static WorkerThreadBuilder builder() {
    return new WorkerThreadBuilder();
  }
}
