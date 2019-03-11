package com.obsidiandynamics.flux;

/**
 *  Calculates the integral of a straight line, sampled between the origin
 *  and a point {@code x}. The shape is a trapezium, hence the name of this
 *  class.
 */
final class Trapezium {
  private Trapezium() {}
  
  /**
   *  Computes the integral, taking in the y-intercept, the gradient of the line
   *  and the upper bound on the x value (lower bound being the origin). <p>
   *  
   *  <pre>
   *             |     /
   *             |    /
   *             |   /
   *             |  /|
   *             | / |
   *  yIntercept_|/<------ gradient  
   *             |   |
   *             |   | 
   *             |___|________
   *             ^   ^
   *        origin    xBound
   *
   *  </pre>
   *  @param yIntercept The y-intercept.
   *  @param gradient The gradient of the line.
   *  @param xBound The upper bound on x.
   *  @return The integral value.
   */
  static double integrate(double yIntercept, double gradient, double xBound) {
    return gradient * xBound * xBound / 2 + yIntercept * xBound;
  }
}
