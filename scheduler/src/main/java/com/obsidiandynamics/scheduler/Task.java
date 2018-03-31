package com.obsidiandynamics.scheduler;

public interface Task {
  /**
   *  Obtains the scheduled execution time, in absolute nanoseconds. 
   *  See {@link System#nanoTime()}.
   *  
   *  @return The scheduled execution time.
   */
  long getTime();
  
  /**
   *  Obtains the task's unique identifier.
   *  
   *  @return The task's ID.
   */
  Comparable<?> getId();
  
  /**
   *  Executes the task.
   *  
   *  @param scheduler The executing scheduler.
   */
  void execute(TaskScheduler scheduler);
}