package com.obsidiandynamics.junit;

import static org.hamcrest.MatcherAssert.*;

import java.util.*;

import org.hamcrest.collection.*;
import org.hamcrest.core.*;
import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class TestCycleTest {
  @Test
  public void testConformance() throws Exception {
    Assertions.assertUtilityClassWellDefined(TestCycle.class);
  }
  
  @Test
  public void testOnce() {
    final List<Object[]> params = TestCycle.once();
    assertThat(params, IsCollectionWithSize.hasSize(1));
    assertThat(params, IsCollectionContaining.hasItem(new Object[0]));
  }
  
  @Test
  public void testTimesQuietly() {
    final List<Object[]> params = TestCycle.timesQuietly(1);
    assertThat(params, IsCollectionWithSize.hasSize(1));
    assertThat(params, IsCollectionContaining.hasItem(new Object[0]));
  }
  
  @Test
  public void testTimes() {
    final List<Object[]> params = TestCycle.times(1);
    assertThat(params, IsCollectionWithSize.hasSize(1));
    assertThat(params, IsCollectionContaining.hasItem(new Object[] { 0 }));
  }
}
