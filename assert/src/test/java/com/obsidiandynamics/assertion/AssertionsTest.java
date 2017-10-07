package com.obsidiandynamics.assertion;

import static org.junit.Assert.*;

import org.junit.*;

public final class AssertionsTest {
  @Test
  public void testEnabled() {
    assertTrue(Assertions.areEnabled());
  }
  
  @Test
  public void testAssertRunnableTrue() {
    assertTrue(Assertions.assertRunnable(() -> {}));
  }
  
  @Test
  public void testSelfConformance() throws Exception {
    Assertions.assertUtilityClassWellDefined(Assertions.class);
  }
  
  @Test(expected=AssertionError.class)
  public void testConformanceNonFinal() throws Exception {
    class NonFinal {}
    Assertions.assertUtilityClassWellDefined(NonFinal.class);
  }
  
  @Test(expected=AssertionError.class)
  public void testConformanceMultipleConstructors() throws Exception {
    final class MultipleConstructors {
      @SuppressWarnings("unused")
      MultipleConstructors(String s) {}
      
      @SuppressWarnings("unused")
      MultipleConstructors(int i) {}
    }
    Assertions.assertUtilityClassWellDefined(MultipleConstructors.class);
  }
  
  static final class NonPrivateConstructor {
    NonPrivateConstructor() {}
  }
  
  @Test(expected=AssertionError.class)
  public void testConformanceNonPrivateConstructor() throws Exception {
    Assertions.assertUtilityClassWellDefined(NonPrivateConstructor.class);
  }
  
  static final class NonStaticMethods {
    private NonStaticMethods() {}
    
    void instanceMethod() {}
  }
  
  @Test(expected=AssertionError.class)
  public void testConformanceNonStaticMethods() throws Exception {
    Assertions.assertUtilityClassWellDefined(NonStaticMethods.class);
  }
  
  @Test
  public void testToStringOverridePass() {
    class ToStringOverriden {
      @Override public String toString() { return "toString()"; }
    }
    Assertions.assertToStringOverride(new ToStringOverriden());
  }
  
  @Test(expected=AssertionError.class)
  public void testToStringOverridenFail() {
    Assertions.assertToStringOverride(new Object());
  }
  
  @Test
  public void testAssertTrue() {
    Assertions.asserterOfTrue(true).run();
  }
  
  @Test(expected=AssertionError.class)
  public void testAssertTrueFail() {
    Assertions.asserterOfTrue(false).run();
  }
  
  @Test
  public void testNot() {
    assertTrue(Assertions.not(Assertions.not(true)));
  }
}
