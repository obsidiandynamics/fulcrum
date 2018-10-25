package com.obsidiandynamics.junit;

import org.junit.rules.*;
import org.junit.runner.*;
import org.junit.runners.model.*;

/**
 *  A (class-level) rule for tracking the pass/fail status of the encompassing test class. <p>
 *  
 *  This rule is useful in scenarios where the individual test methods in a test class
 *  are not completely isolated. For example, they may need to be executed in a particular 
 *  sequence, or where a failure of one test method should be sufficient to abort the 
 *  remaining tests(perhaps the failure of one test leaves the system in a debuggable state, 
 *  and running further tests beyond the point of failure is generally undesirable). <p>
 *  
 *  Once the rule added to the class, each method should test its status by invoking
 *  {@link TestStatus#isFailed()} and passing the result to 
 *  {@link org.junit.Assume#assumeFalse(boolean)}. In this manner, the subsequent tests will
 *  be aborted but will not be marked as failed. <p>
 *  
 *  <b>Usage</b>:<br>
 *  
 *  Add the following to the test class:<br>
 *  <pre>
 *  {@literal @}ClassRule
 *  public static final TestStatus testStatus = new TestStatus();
 *     
 *  {@literal @}Rule
 *  public final TestWatcher __testStatusWatcher = testStatus.getWatcher();
 *  </pre>
 *  
 *  And the following to the beginning of each test method:<br>
 *  <pre>
 *  assumeFalse(testStatus.isFailed());
 *  </pre>
 *  
 *  <b>Note</b>: the double underscore is used to denote an unused variable that requires to be declared
 *  and annotated. <p>
 *  
 *  When combining multiple rules, for example a {@link TestStatus} with an {@link ExpectedException},
 *  the order of rule execution may be important. If a rule is used to verify exceptions (e.g. 
 *  {@link ExpectedException}) then the latter should be given an opportunity to trap the exception
 *  <em>before</em> evaluating the {@link TestWatcher} produced by {@link TestStatus#getWatcher()}. 
 *  Otherwise, if the {@link TestWatcher} is evaluated first, an expected exception would be treated as a 
 *  failure, preventing other rules from executing. Use a {@link RuleChain} to prescribe the
 *  evaluation order, as shown in in the example below.
 *  
 *  <pre>
 *  {@literal @}ClassRule
 *  public static final TestStatus testStatus = new TestStatus();
 *  
 *  private final TestWatcher testStatusWatcher = testStatus.getWatcher();
 *   
 *  private final ExpectedException expectedException = ExpectedException.none();
 *   
 *  {@literal @}Rule
 *  public final TestRule __chain = RuleChain.outerRule(testStatusWatcher).around(expectedException);
 *  </pre>
 */
public final class TestStatus implements TestRule {
  private boolean failed;
  
  @Override
  public Statement apply(Statement base, Description description) {
    return base;
  }
  
  public TestWatcher getWatcher() {
    return new TestWatcher() {
      @Override
      protected void failed(Throwable e, Description description) {
        failed = true;
      }
    };
  }
  
  public boolean isFailed() {
    return failed;
  }
}
