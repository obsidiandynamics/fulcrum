package com.obsidiandynamics.flux;

import static org.junit.Assert.*;

import org.assertj.core.api.*;
import org.junit.*;

import com.obsidiandynamics.verifier.*;

import nl.jqno.equalsverifier.*;

public final class RateTest {
  @Test
  public void testPojo() {
    PojoVerifier.forClass(Rate.class)
    .constructorArgs(new ConstructorArgs()
                     .with(double.class, 1000.0)
                     .with(double.class, 1500.0)
                     .with(double.class, 10.0)
                     .with(double.class, 50.0))
    .verify();
  }

  @Test
  public void testEqualsHashCode() {
    EqualsVerifier.forClass(Rate.class).verify();
  }

  @Test
  public void testFlatBuilder_success() {
    Rate.flatBuilder().withRate(100).withDuration(10).build();
  }

  @Test
  public void testFlatBuilder_illegalArgument() {
    Assertions.assertThatThrownBy(Rate.flatBuilder().withRate(0).withDuration(10)::build)
    .isInstanceOf(IllegalArgumentException.class).hasMessage("Base rate must be greater than 0");

    Assertions.assertThatThrownBy(Rate.flatBuilder().withRate(1).withDuration(0)::build)
    .isInstanceOf(IllegalArgumentException.class).hasMessage("Duration must be greater than 0");
  }

  @Test
  public void testRampUpBuilder_success() {
    Rate.rampUpBuilder().withBaseRate(5).withPeakRate(10).withRampUpTime(10).withDuration(10).build();
  }

  @Test
  public void testRampUpBuilder_illegalArgument() {
    Assertions.assertThatThrownBy(Rate.rampUpBuilder().withBaseRate(0).withPeakRate(10).withRampUpTime(10).withDuration(10)::build)
    .isInstanceOf(IllegalArgumentException.class).hasMessage("Base rate must be greater than 0");

    Assertions.assertThatThrownBy(Rate.rampUpBuilder().withBaseRate(10).withPeakRate(6).withRampUpTime(10).withDuration(10)::build)
    .isInstanceOf(IllegalArgumentException.class).hasMessage("Base rate cannot exceed peak rate");

    Assertions.assertThatThrownBy(Rate.rampUpBuilder().withBaseRate(10).withPeakRate(10).withRampUpTime(12).withDuration(10)::build)
    .isInstanceOf(IllegalArgumentException.class).hasMessage("Ramp up time cannot exceed total duration");
  }

  @Test
  public void testScaleRate_flat() {
    final Rate rate = Rate.flatBuilder().withRate(100).withDuration(30).build();
    final Rate scaled = rate.scaleRate(2);
    assertEquals(200, scaled.getBaseRate(), Double.MIN_VALUE);
    assertEquals(200, scaled.getPeakRate(), Double.MIN_VALUE);
    assertEquals(rate.getRampUpTime(), scaled.getRampUpTime(), 0);
    assertEquals(rate.getDuration(), scaled.getDuration(), 0);
  }

  @Test
  public void testScaleRate_rampUp() {
    final Rate rate = Rate.rampUpBuilder().withBaseRate(50).withRampUpTime(10).withPeakRate(100).withDuration(30).build();
    final Rate scaled = rate.scaleRate(2);
    assertEquals(100, scaled.getBaseRate(), Double.MIN_VALUE);
    assertEquals(200, scaled.getPeakRate(), Double.MIN_VALUE);
    assertEquals(rate.getRampUpTime(), scaled.getRampUpTime(), 0);
    assertEquals(rate.getDuration(), scaled.getDuration(), 0);
  }

  @Test
  public void testScaleTime_flat() {
    final Rate rate = Rate.flatBuilder().withRate(100).withDuration(30).build();
    final Rate scaled = rate.scaleTime(2);
    assertEquals(rate.getBaseRate(), scaled.getBaseRate(), 0);
    assertEquals(rate.getPeakRate(), scaled.getPeakRate(), 0);
    assertEquals(60, scaled.getRampUpTime(), Double.MIN_VALUE);
    assertEquals(60, scaled.getDuration(), Double.MIN_VALUE);
  }

  @Test
  public void testScaleTime_rampUp() {
    final Rate rate = Rate.rampUpBuilder().withBaseRate(50).withRampUpTime(10).withPeakRate(100).withDuration(30).build();
    final Rate scaled = rate.scaleTime(2);
    assertEquals(rate.getBaseRate(), scaled.getBaseRate(), 0);
    assertEquals(rate.getPeakRate(), scaled.getPeakRate(), 0);
    assertEquals(20, scaled.getRampUpTime(), Double.MIN_VALUE);
    assertEquals(60, scaled.getDuration(), Double.MIN_VALUE);
  }
  
  @Test
  public void testComputeVolume() {
    final Rate rate = Rate.rampUpBuilder().withBaseRate(50).withRampUpTime(10).withPeakRate(100).withDuration(30).build();
    
    assertEquals(0, rate.computeVolume(0));
    assertEquals(750, rate.computeVolume(10));
    assertEquals(1750, rate.computeVolume(20));
    assertEquals(2750, rate.computeVolume(30));
    assertEquals(2850, rate.computeVolume(31));
    assertEquals(2750, rate.computeTotal());
  }
  
  @Test
  public void testComputeRate() {
    final Rate rate = Rate.rampUpBuilder().withBaseRate(50).withRampUpTime(10).withPeakRate(100).withDuration(30).build();
    
    assertEquals(50, rate.computeRate(0), Double.MIN_VALUE);
    assertEquals(100, rate.computeRate(10), Double.MIN_VALUE);
    assertEquals(100, rate.computeRate(20), Double.MIN_VALUE);
    assertEquals(100, rate.computeRate(30), Double.MIN_VALUE);
    assertEquals(100, rate.computeRate(31), Double.MIN_VALUE);
  }
}
