package com.obsidiandynamics.worker;

import static com.obsidiandynamics.func.Functions.*;

import java.util.*;

public final class WorkerThread implements Terminable, Joinable {
  private final Thread driver;
  
  private final WorkerCycle worker;
  
  private final WorkerStartup onStartup;
  
  private final WorkerShutdown onShutdown;
  
  private final WorkerExceptionHandler onUncaughtException;
  
  private volatile WorkerState state = WorkerState.CONCEIVED;
  
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
    
    ifPresentVoid(options.getName(), driver::setName);
    
    driver.setDaemon(options.isDaemon());
    driver.setPriority(options.getPriority());
  }
  
  public Thread getDriverThread() {
    return driver;
  }
  
  /**
   *  Starts the worker thread.
   */
  public void start() {
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
  public Joinable terminate() {
    synchronized (stateLock) {
      if (state == WorkerState.CONCEIVED) {
        state = WorkerState.TERMINATED;
      } else if (state == WorkerState.RUNNING) {
        driver.interrupt();
        state = WorkerState.TERMINATING;
      }
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
        // Interrupt may have been set as part of terminating the thread; clear it before proceeding with the
        // shutdown handler.
        Thread.interrupted(); 
        
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
  public WorkerState getState() {
    return state;
  }
  
  private void cycle() throws InterruptedException {
    worker.cycle(this);
  }
  
  @Override
  public boolean join(long timeoutMillis) throws InterruptedException {
    driver.join(timeoutMillis);
    return ! driver.isAlive();
  }
  
  public String getName() {
    return driver.getName();
  }
  
  public boolean isDaemon() {
    return driver.isDaemon();
  }
  
  public int getPriority() {
    return driver.getPriority();
  }
  
  @Override
  public int hashCode() {
    return Objects.hashCode(driver);
  }

  @Override
  public boolean equals(Object obj) {
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
  public String toString() {
    return WorkerThread.class.getSimpleName() + " [thread=" + driver + ", state=" + state + "]";
  }
  
  public static WorkerThreadBuilder builder() {
    return new WorkerThreadBuilder();
  }
}
