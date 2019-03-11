package com.obsidiandynamics.flux;

import static com.obsidiandynamics.func.Functions.*;

public final class Rate {
  /** Base emission rate, in events/sec. */
  private final double baseRate;

  /** Peak emission rate, in events/sec. */
  private final double peakRate;

  /** Base-to-peak ramp-up time, in seconds. */
  private final double rampUpTime;

  /** Duration, in seconds. */
  private final double duration;

  public Rate(double baseRate, 
              double peakRate, 
              double rampUpTime, 
              double duration) {
    mustBeGreater(baseRate, 0, illegalArgument("Base rate must be greater than 0"));
    this.baseRate = baseRate;
    if (peakRate != 0) {
      mustBeLessOrEqual(baseRate, peakRate, illegalArgument("Base rate cannot exceed peak rate"));
      mustBeLessOrEqual(rampUpTime, duration, illegalArgument("Ramp up time cannot exceed total duration"));
      this.peakRate = peakRate;
      this.rampUpTime = rampUpTime;
    } else {
      this.peakRate = baseRate;
      this.rampUpTime = duration;
    }
    
    mustBeGreater(duration, 0, illegalArgument("Duration must be greater than 0"));
    this.duration = duration;
  }
  
  public static final class FlatRateBuilder {
    private double rate;
    
    private double duration;
    
    public FlatRateBuilder withRate(double rate) {
      this.rate = rate;
      return this;
    }

    public FlatRateBuilder withDuration(double duration) {
      this.duration = duration;
      return this;
    }

    public Rate build() {
      return new Rate(rate, 0, 0, duration);
    }
  }
  
  public static final FlatRateBuilder flatBuilder() {
    return new FlatRateBuilder();
  }
  
  public static final class RampUpRateBuilder {
    private double baseRate;

    private double peakRate;

    private double rampUpTime;

    private double duration;

    public RampUpRateBuilder withBaseRate(double baseRate) {
      this.baseRate = baseRate;
      return this;
    }

    public RampUpRateBuilder withPeakRate(double peakRate) {
      this.peakRate = peakRate;
      return this;
    }

    public RampUpRateBuilder withRampUpTime(double rampUpTime) {
      this.rampUpTime = rampUpTime;
      return this;
    }

    public RampUpRateBuilder withDuration(double duration) {
      this.duration = duration;
      return this;
    }
    
    public Rate build() {
      return new Rate(baseRate, peakRate, rampUpTime, duration);
    }
  }
  
  public static RampUpRateBuilder rampUpBuilder() {
    return new RampUpRateBuilder();
  }

  public final double getBaseRate() {
    return baseRate;
  }

  public final double getPeakRate() {
    return peakRate;
  }

  public final double getRampUpTime() {
    return rampUpTime;
  }

  public final double getDuration() {
    return duration;
  }

  /**
   *  Scales the rate (base and peak) components independently of the time (ramp-up and duration).
   *  
   *  @param scale The scale to apply.
   *  @return The scaled {@link Rate} instance.
   */
  public Rate scaleRate(double scale) {
    return new Rate(baseRate * scale, peakRate * scale, rampUpTime, duration);
  }

  /**
   *  Scales the time (ramp-up and duration) components independently of the rate (base and peak).
   *  
   *  @param scale The scale to apply.
   *  @return The scaled {@link Rate} instance.
   */
  public Rate scaleTime(double scale) {
    return new Rate(baseRate, peakRate, rampUpTime * scale, duration * scale);
  }
  
  /**
   *  Computes the total number of events that should be emitted by end of the timeline
   *  implied by this {@link Rate}.
   *  
   *  @return The total number of events.
   */
  public long computeTotal() {
    return computeVolume(duration);
  }
  
  /**
   *  Computes the number of events that should have been emitted from the beginning to the time offset 
   *  specified by {@code elapsedSeconds}. <p>
   *  
   *  The time offset can extend beyond the expected duration, in which case the volume is 
   *  extrapolated as if the timeline had been extended.
   *  
   *  @param elapsedSeconds The number of seconds elapsed.
   *  @return The number of events emitted by that point in time.
   */
  public long computeVolume(double elapsedSeconds) {
    mustBeGreaterOrEqual(elapsedSeconds, 0, illegalArgument("Elapsed seconds must be non-negative"));
    final double secondsDuringRampUp = Math.min(elapsedSeconds, rampUpTime);
    final double secondsAfterPeak = Math.max(0, elapsedSeconds - rampUpTime);
    final double gradient = (peakRate - baseRate) / rampUpTime;
    return (long) (Trapezium.integrate(baseRate, gradient, secondsDuringRampUp) + Trapezium.integrate(peakRate, 0, secondsAfterPeak));
  }
  
  /**
   *  Computes the rate of emission that applies to the time offset specified by {@code elapsedSeconds}. <p>
   *  
   *  The time offset can extend beyond the expected duration, in which case the peak rate is returned.
   *  
   *  @param elapsedSeconds The number of seconds elapsed.
   *  @return The emission rate at that point in time.
   */
  public double computeRate(double elapsedSeconds) {
    mustBeGreaterOrEqual(elapsedSeconds, 0, illegalArgument("Elapsed seconds must be non-negative"));
    final double peakRatio = Math.min(elapsedSeconds / rampUpTime, 1);
    return baseRate + peakRatio * (peakRate - baseRate);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Double.hashCode(baseRate);
    result = prime * result + Double.hashCode(duration);
    result = prime * result + Double.hashCode(peakRate);
    result = prime * result + Double.hashCode(rampUpTime);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof Rate) {
      final Rate that = (Rate) obj;
      return Double.compare(baseRate, that.baseRate) == 0 && 
          Double.compare(duration, that.duration) == 0 && 
          Double.compare(peakRate, that.peakRate) == 0 && 
          Double.compare(rampUpTime, that.rampUpTime) == 0;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return Rate.class.getSimpleName() + " [baseRate=" + baseRate + ", peakRate=" + peakRate + 
        ", rampUpTime=" + rampUpTime + ", duration=" + duration + "]";
  }
}
