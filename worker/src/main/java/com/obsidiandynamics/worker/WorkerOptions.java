package com.obsidiandynamics.worker;

import com.obsidiandynamics.concat.*;

public final class WorkerOptions {
  private String name;
  
  private boolean daemon;
  
  private int priority = Thread.NORM_PRIORITY;

  public String getName() {
    return name;
  }

  public WorkerOptions withName(String name) {
    this.name = name;
    return this;
  }
  
  /**
   *  Helper for naming the thread by taking the simple name of the given class (i.e. {@link Class#getSimpleName()})
   *  and concatenating hyphen-delimited {@code nameFrags}.<p>
   *  
   *  Example 1: {@code withName(Reaper.class)} results in {@code Reaper}.<br>
   *  Example 2: {@code withName(Reaper.class, "collector", 0)} results in {@code Reaper-collector-0}.<br>
   *  
   *  @param cls The class name.
   *  @param nameFrags The name fragments.
   *  @return This {@link WorkerOptions} instance for fluent chaining.
   */
  public WorkerOptions withName(Class<?> cls, Object... nameFrags) {
    final String name = new Concat()
        .append(cls.getSimpleName())
        .when(nameFrags.length > 0).append(new Concat().append("-").appendArray("-", nameFrags))
        .toString();
    return withName(name);
  }

  public boolean isDaemon() {
    return daemon;
  }
  
  /**
   *  A shortcut way of calling {@code withDaemon(true)}.
   *  
   *  @return This {@link WorkerOptions} instance for fluent chaining.
   */
  public WorkerOptions daemon() {
    return withDaemon(true);
  }

  public WorkerOptions withDaemon(boolean daemon) {
    this.daemon = daemon;
    return this;
  }

  public int getPriority() {
    return priority;
  }

  public WorkerOptions withPriority(int priority) {
    this.priority = priority;
    return this;
  }

  @Override
  public String toString() {
    return WorkerOptions.class.getSimpleName() + " [name=" + name + ", daemon=" + daemon + ", priority=" + priority + "]";
  }
}
