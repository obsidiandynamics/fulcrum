package com.obsidiandynamics.flux;

import static org.junit.Assert.*;

import org.junit.*;

public final class FluxExceptionTest {
  @Test
  public void testWrapException_withFluxException() {
    final FluxException cause = new FluxException("Simulated", null);
    assertSame(cause, FluxException.wrap(cause));
  }
  
  @Test
  public void testWrapException_withNull() {
    assertNull(FluxException.wrap(null));
  }
  
  @Test
  public void testWrapException_withNonFluxException() {
    final Exception cause = new Exception("Simulated");
    final FluxException wrapped = FluxException.wrap(cause);
    org.assertj.core.api.Assertions.assertThat(wrapped).isInstanceOf(FluxException.class).hasCause(cause);
  }
  
  @Test
  public void testWrapException_withInterruptedException() {
    assertNull(FluxException.wrap(new InterruptedException("Simulated")));
  }
}
