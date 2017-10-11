package com.obsidiandynamics.junit;

import java.util.*;

/**
 *  Generates parameters for JUnit's {@code Parameterized} class
 *  in a way that the test case can be repeated a set number of times.
 */
public final class TestCycle {
  private TestCycle() {}
  
  /**
   *  Produces a singleton list with no parameters, effectively a single execution
   *  cycle.
   *  
   *  @return The parameter data {@link List}.
   */
  public static List<Object[]> once() {
    return timesQuietly(1);
  }

  /**
   *  This method assumes that you don't care about the run number, and so generates
   *  a list of zero-length arrays.
   *  
   *  @param times The number of times to repeat.
   *  @return The parameter data {@link List}.
   */
  public static List<Object[]> timesQuietly(int times) {
    return Arrays.asList(new Object[times][0]);
  }
  
  /**
   *  This method assumes that you have an integer variable annotated with 
   *  {@code @Parameter(0)}, to which a zero-based run number will be assigned
   *  at the beginning of each iteration.
   *  
   *  @param times The number of times to repeat.
   *  @return The parameter data {@link List}.
   */
  public static List<Object[]> times(int times) {
    final Object[][] params = new Object[times][1];
    for (int i = 0; i < times; i++) params[i][0] = i;
    return Arrays.asList(params);
  }
}
