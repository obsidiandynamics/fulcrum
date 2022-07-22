package com.obsidiandynamics.flux;

import static org.junit.Assert.*;

import java.util.function.*;

import org.junit.*;

import com.obsidiandynamics.verifier.*;

import nl.jqno.equalsverifier.*;

public final class TimestampedTest {
  @Test
  public void testPojo() {
    PojoVerifier.forClass(Timestamped.class).verify();
  }
  
  @Test
  public void testEqualsHashCode() {
    EqualsVerifier.forClass(Timestamped.class).verify();
  }

  @Test
  public void testMapPreserve() {
    final Timestamped<Integer> in = new Timestamped<>(42);
    final Function<Timestamped<Integer>, Timestamped<String>> map = Timestamped.mapPreserve(String::valueOf);
    final Timestamped<String> out = map.apply(in);
    assertEquals("42", out.getValue());
    assertEquals("in=" + in + ", out=" + out, in.getTimestamp(), out.getTimestamp());
  }

  @Test
  public void testMapRestamp() {
    final Timestamped<Integer> in = new Timestamped<>(42);
    final Function<Timestamped<Integer>, Timestamped<String>> map = Timestamped.mapRestamp(String::valueOf);
    while (System.nanoTime() <= in.getTimestamp());
    final Timestamped<String> out = map.apply(in);
    assertEquals("42", out.getValue());
    assertTrue("in=" + in + ", out=" + out, out.getTimestamp() > in.getTimestamp());
  }
}
