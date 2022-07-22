package com.obsidiandynamics.scheduler;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import com.obsidiandynamics.worker.*;

/**
 *  A scheduler for dispatching arbitrary tasks.
 */
public final class TaskScheduler implements Terminable, Joinable {
  /** Maximum sleep time. If the next task's time is longer, the sleep will be performed in a loop.
   *  This is also the default time that the scheduler sleeps for if it has no pending tasks. */
  private static final long MAX_SLEEP_NANOS = 1_000_000_000L;
  
  /** Minimum sleep time. Below this threshold sleeping isn't worthwhile. */
  private static final long MIN_SLEEP_NANOS = 1_000_000L;
  
  /** Compensation for the overhead of scheduling a task. Use a small positive number if the tasks
   *  are coming in later than desired. */
  private static final long ADJ_NANOS = 0L;
  
  /** List of pending tasks, ordered with the most immediate at the head. */
  private final NavigableSet<Task> tasks = new ConcurrentSkipListSet<>(TaskScheduler::compareByTimeAndId);
  
  @SuppressWarnings("unchecked")
  static int compareByTimeAndId(Task t1, Task t2) {
    final int timeComp = Long.compare(t1.getTime(), t2.getTime());
    if (timeComp != 0) {
      return timeComp;
    } else {
      @SuppressWarnings("rawtypes")
      final Comparable c1 = t1.getId();
      @SuppressWarnings("rawtypes")
      final Comparable c2 = t2.getId();
      return c1.compareTo(c2);
    }
  }
  
  /** Lock for the scheduler thread to sleep on; can be used to wake the thread. */
  private final Object sleepLock = new Object();
  
  /** The worker thread for performing task execution. */
  private final WorkerThread executor;
  
  /** The time when the thread should be woken, in absolute nanoseconds. See {@link System#nanoTime()}. */
  private volatile long nextWake;
  
  /** Whether execution should be forced for all tasks (regarding of their scheduled time), pending and future. */
  private volatile boolean forceExecute;
  
  /** Atomically assigns numbers for scheduler thread naming. */
  private static final AtomicInteger nextSchedulerThreadNo = new AtomicInteger();
  
  public TaskScheduler() {
    this(TaskScheduler.class.getSimpleName() + "-" + nextSchedulerThreadNo.getAndIncrement());
  }
  
  public TaskScheduler(String threadName) {
    executor = WorkerThread.builder()
        .withOptions(new WorkerOptions().daemon().withName(threadName))
        .onCycle(this::cycle)
        .build();
  }
  
  public void start() {
    executor.start();
  }
  
  public void clear() {
    tasks.clear();
  }
  
  /**
   *  Terminates the scheduler, shutting down the worker thread and preventing further 
   *  task executions.
   *  
   *  @return A {@link Joinable} for the caller to wait on.
   */
  @Override
  public Joinable terminate() {
    executor.terminate();
    return this;
  }
  
  @Override
  public boolean join(long timeoutMillis) throws InterruptedException {
    return executor.join(timeoutMillis);
  }
  
  private void cycle(WorkerThread thread) {
    synchronized (sleepLock) {
      try {
        delay(tasks.first().getTime());
      } catch (NoSuchElementException e) {
        delay(System.nanoTime() + MAX_SLEEP_NANOS);
      }
    }

    scheduleSingle(tasks, this, forceExecute);
  }
  
  /**
   *  Schedules a single task from the head of the queue, if one is pending and its 
   *  time has come.<p>
   *  
   *  This code has been extracted into a static method to facilitate unit test coverage,
   *  which is otherwise non-deterministic due to the double-checked use of {@code first()}
   *  and {@code remove()}.
   *  
   *  @param tasks The tasks list.
   *  @param scheduler The scheduler.
   *  @param forceExecute Whether all tasks should be executed, regardless of time.
   */
  static void scheduleSingle(NavigableSet<Task> tasks, TaskScheduler scheduler, boolean forceExecute) {
    try {
      final Task first = tasks.first();
      if (forceExecute || System.nanoTime() >= first.getTime() - ADJ_NANOS) {
        if (tasks.remove(first)) {
          first.execute(scheduler);
        }
      }
    } catch (NoSuchElementException ignored) {} // in case the task was aborted in the meantime
  }
  
  /**
   *  Schedules a task for execution.
   *  
   *  @param task The task to schedule.
   */
  public void schedule(Task task) {
    tasks.add(task);
    if (task.getTime() < nextWake) {
      adjustWakeHorizon(task);
    }
  }
  
  /**
   *  Conditionally brings the wake horizon forward if the task is sooner
   *  than the wake horizon.<p>
   *  
   *  This method has been extracted to facilitate unit testing, which is otherwise
   *  non-deterministic due to the use of double-checking in {@link #schedule(Task)}.
   *  
   *  @param task The task to be scheduled.
   */
  void adjustWakeHorizon(Task task) {
    synchronized (sleepLock) {
      if (task.getTime() < nextWake) {
        nextWake = task.getTime();
        sleepLock.notify();
      }
    }
  }
  
  /**
   *  Removes the given task from the schedule. Once definitively removed, the scheduler will
   *  not execute the task.
   *  
   *  @param task The task to abort.
   *  @return Whether the task was in the schedule (and hence was removed).
   */
  public boolean abort(Task task) {
    return tasks.remove(task);
  }
  
  /**
   *  Puts the scheduler thread to sleep until the given time.<p>
   *  
   *  Requires {@code sleepLock} to be acquired before calling.
   *  
   *  @param until The wake time, in absolute nanoseconds (see {@link System#nanoTime()}).
   */
  private void delay(long until) {
    nextWake = until;
    while (! forceExecute) {
      final long timeDiff = Math.min(MAX_SLEEP_NANOS, nextWake - System.nanoTime() - ADJ_NANOS);
      try {
        if (timeDiff >= MIN_SLEEP_NANOS) {
          final long millis = timeDiff / 1_000_000L;
          final int nanos = (int) (timeDiff - millis * 1_000_000L);
          sleepLock.wait(millis, nanos);
        } else {
          break;
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }
  
  /**
   *  Forces the immediate execution of all pending tasks, and all future tasks yet to be enqueued.
   */
  public void forceExecute() {
    forceExecute = true;
    synchronized (sleepLock) {
      sleepLock.notify();
    }
  }
  
  /**
   *  Forces the execution of a given task.<p>
   *  
   *  This method is asynchronous, returning as soon as the resulting signal is enqueued.
   *  
   *  @param task The task to time out.
   */
  public void executeNow(Task task) {
    if (abort(task)) {
      task.execute(this);
    }
  }
}
