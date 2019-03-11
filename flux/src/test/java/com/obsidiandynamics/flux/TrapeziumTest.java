package com.obsidiandynamics.flux;

import static org.junit.Assert.*;

import org.junit.*;

public final class TrapeziumTest {
  @Test
  public void testIntegrate_ascending() {
    //             .
    //            /|
    //           / |
    //          /  | 
    //         /   |
    //        /|   |
    //       /%|   | 150
    //      /%%|   |
    //     |%%%|   |
    // 100 |%%%|   | 
    //     |%%%|___|
    //
    //     |<-10-->|
    //
    final int gradient = (150 - 100) / 10;
    assertEquals(0, Trapezium.integrate(100, gradient, 0), Double.MIN_VALUE);
    assertEquals(1250, Trapezium.integrate(100, gradient, 10), Double.MIN_VALUE);
    assertEquals(562.5, Trapezium.integrate(100, gradient, 5), Double.MIN_VALUE);
  }
  
  @Test
  public void testIntegrate_flat() {
    //      _______
    //     |%%%|   |
    // 100 |%%%|   | 100
    //     |%%%|___|
    //
    //     |<-10-->|
    //
    assertEquals(0, Trapezium.integrate(100, 0, 0), Double.MIN_VALUE);
    assertEquals(1000, Trapezium.integrate(100, 0, 10), Double.MIN_VALUE);
    assertEquals(500, Trapezium.integrate(100, 0, 5), Double.MIN_VALUE);
  }
}
