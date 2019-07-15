package com.obsidiandynamics.threads;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.assertj.core.api.*;
import org.junit.*;
import org.mockito.*;

import com.obsidiandynamics.func.*;

public final class StripedTest {
  @Test
  public void testGet() {
    final Striped<Object> striped = Classes.cast(mock(Striped.class, Answers.CALLS_REAL_METHODS));
    final Object value = new Object();
    when(striped.get(anyInt())).thenReturn(value);

    final String key = "someKey";
    assertSame(value, striped.get(key));
    verify(striped).get(eq(key.hashCode()));
  }

  @Test
  public void testResolveStripe() {
    final int stripes = 16;
    for (int i = -stripes * 2; i <= stripes * 2; i++) {
      final int stripe = Striped.resolveStripe(i, 16);
      Assertions.assertThat(stripe).isGreaterThanOrEqualTo(0);
      Assertions.assertThat(stripe).isLessThan(stripes);
    }
  }
}
