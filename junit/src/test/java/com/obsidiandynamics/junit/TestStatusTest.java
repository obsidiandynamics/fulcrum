package com.obsidiandynamics.junit;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runners.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class TestStatusTest {
  @ClassRule
  public static final TestStatus testStatus = new TestStatus();
  
  private final TestWatcher testStatusWatcher = testStatus.getWatcher();
  
  private final ExpectedException expectedException = ExpectedException.none();
  
  /**
   *  Note: normally, the correct approach is to have {@code testStatusWatcher} as an outer
   *  rule, around the {@code expectedException}. We've done the opposite here only to satisfy
   *  the test, allowing {@link #_2_testThatCreatesAFault()} to signal a fault condition to
   *  {@code testStatusWatcher} without allowing the fault to propagate to the outer runner
   *  (just so that the test doesn't appear to have actually failed).
   */
  @Rule
  public final TestRule __chain = RuleChain.outerRule(expectedException).around(testStatusWatcher);

  @Test
  public void _0_testThatStatusIsPassing() {
    assertTrue(! testStatus.isFailed());
  }

  @Test
  public void _1_testThatStatusIsStillPassingAfterOneTest() {
    assertTrue(! testStatus.isFailed());
  }
  
  @Test
  public void _2_testThatCreatesAFault() throws IOException {
    expectedException.expect(IOException.class);
    throw new IOException("Simulated");
  }
  
  @Test
  public void _3_testThatStatusIsFailingAfterAFailedTest() {
    assertTrue(testStatus.isFailed());
  }
}
