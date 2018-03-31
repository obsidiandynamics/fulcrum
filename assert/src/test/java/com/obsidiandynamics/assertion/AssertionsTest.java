package com.obsidiandynamics.assertion;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
  public void testSelfConformance() {
    Assertions.assertUtilityClassWellDefined(Assertions.class);
  }
  
  @Test(expected=AssertionError.class)
  public void testConformanceNonFinal() {
    class NonFinal {}
    Assertions.assertUtilityClassWellDefined(NonFinal.class);
  }
  
  @Test(expected=AssertionError.class)
  public void testConformanceMultipleConstructors() {
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
  public void testConformanceNonPrivateConstructor() {
    Assertions.assertUtilityClassWellDefined(NonPrivateConstructor.class);
  }
  
  static final class NonStaticMethods {
    private NonStaticMethods() {}
    
    void instanceMethod() {}
  }
  
  @Test(expected=AssertionError.class)
  public void testConformanceNonStaticMethods() {
    Assertions.assertUtilityClassWellDefined(NonStaticMethods.class);
  }
  
  static final class ErrorInInitializer {
    private ErrorInInitializer() {
      throw new RuntimeException("test exception");
    }
  }
  
  @Test(expected=AssertionError.class)
  public void testConformanceReflectionError() {
    Assertions.assertUtilityClassWellDefined(ErrorInInitializer.class);
  }
  
  @Test
  public void testToStringOverridePass() {
    class ToStringOverridden {
      @Override public String toString() { return "toString()"; }
    }
    Assertions.assertToStringOverride(new ToStringOverridden());
  }
  
  @Test(expected=AssertionError.class)
  public void testToStringOverriddenFail() {
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
  
  @Test
  public void testRunIfTrue() {
    final Runnable r = mock(Runnable.class);
    Assertions.runIf(() -> true, r);
    verify(r).run();
  }
  
  @Test
  public void testRunIfFalse() {
    final Runnable r = mock(Runnable.class);
    Assertions.runIf(() -> false, r);
    verifyNoMoreInteractions(r);
  }
  
  @Test
  public void testRunIfEnabled() {
    final Runnable r = mock(Runnable.class);
    Assertions.runIfEnabled(r);
    verify(r).run();
  }
  
  @Test
  public void testAssertConditionPass() {
    Assertions.assertCondition(() -> true, "will not be thrown");
  }
  
  @Test(expected=AssertionError.class)
  public void testAssertConditionFail() {
    Assertions.assertCondition(() -> false, "will be thrown");
  }
}
