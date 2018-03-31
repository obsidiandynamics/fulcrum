package com.obsidiandynamics.scheduler;

public abstract class AbstractTask<I extends Comparable<I>> implements Task {
  /** The scheduled execution time, in absolute nanoseconds. See {@link System#nanoTime()}. */
  private final long time;
  
  /** The task's unique identifier. */
  private final I id;
  
  public AbstractTask(long time, I id) {
    this.time = time;
    this.id = id;
  }
  
  @Override
  public final long getTime() {
    return time;
  }

  @Override
  public final I getId() {
    return id;
  }
  
  protected final String baseToString() {
    return "time=" + time + ", id=" + id;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " [" + baseToString() + "]";
  }
}