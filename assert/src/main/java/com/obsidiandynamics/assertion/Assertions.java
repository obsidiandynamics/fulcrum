package com.obsidiandynamics.assertion;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

/**
 *  Common assertions.
 */
public final class Assertions {
  private Assertions() {}
  
  /**
   *  Verifies whether assertions are enabled.
   *  
   *  @return True if assertions are enabled.
   */
  public static boolean areEnabled() {
    return not(assertRunnable(asserterOfTrue(false)));
  }
  
  /**
   *  Conditionally runs a given {@code runnable} block of code if
   *  assertions are enabled.
   *  
   *  @param runnable The code to run.
   */
  public static void runIfEnabled(Runnable runnable) {
    runIf(Assertions::areEnabled, runnable);
  }
  
  static Runnable asserterOfTrue(boolean b) {
    return () -> AssertionsEnabledUncovered.doAssert(b);
  }
  
  static boolean not(boolean b) {
    return ! b;
  }
  
  static void runIf(BooleanSupplier condition, Runnable runnable) {
    if (condition.getAsBoolean()) runnable.run();
  }
  
  /**
   *  Asserts a Boolean condition if assertions are enabled. If the condition fails, an
   *  {@link AssertionError} with the given message {@code format} and {@code args}.
   *  
   *  @param condition The condition to test.
   *  @param format The error message format.
   *  @param args Formatting arguments.
   */
  public static void assertCondition(BooleanSupplier condition, String format, Object... args) {
    assertCondition(condition, () -> String.format(format, args));
  }
  
  /**
   *  Asserts a Boolean condition if assertions are enabled. If the condition fails, an
   *  {@link AssertionError} with the message prescribed by {@code messageMaker} is
   *  thrown.
   *  
   *  @param condition The condition to test.
   *  @param messageMaker A way of constructing the error message.
   */
  public static void assertCondition(BooleanSupplier condition, Supplier<String> messageMaker) {
    runIfEnabled(() -> {
      if (! condition.getAsBoolean()) {
        throw new AssertionError(messageMaker.get());
      }
    });
  }
  
  /**
   *  Verifies whether a given {@link Runnable} can complete without throwing an
   *  {@link AssertionError}.
   *  
   *  @param r The {@link Runnable} to test.
   *  @return True if no {@link AssertionError} was thrown.
   */
  public static boolean assertRunnable(Runnable r) {
    try {
      r.run();
      return true;
    } catch (AssertionError e) {
      return false;
    }
  }
  
  /**
   *  Verifies that the given object overrides the {@link Object#toString()} implementation, and that
   *  the implementation operates without throwing any exceptions.
   *
   *  @param obj The object to test.
   */
  public static void assertToStringOverride(Object obj) {
    final String objectToString = obj.getClass().getName() + "@" + Integer.toHexString(obj.hashCode());
    final String actualToString = obj.toString();
    if (Objects.equals(actualToString, objectToString)) {
      throw new AssertionError("The toString() method does not appear to have been overridden");
    }
  }
  
  /**
   *  Verifies that a utility class is well-defined.
   *
   *  Adapted from
   *  <a href="https://github.com/trajano/maven-jee6/blob/master/maven-jee6-test/src/test/java/net/trajano/maven_jee6/test/test/UtilityClassTestUtilTest.java">UtilityClassTestUtilTest</a>.
   *
   *  @param cls Utility class to verify.
   */
  public static void assertUtilityClassWellDefined(Class<?> cls) {
    if (! Modifier.isFinal(cls.getModifiers())) {
      throw new AssertionError("Class must be final");
    }
    
    if (cls.getDeclaredConstructors().length != 1) {
      throw new AssertionError("There must be exactly one construtor");
    }
    
    try {
      final Constructor<?> constructor = cls.getDeclaredConstructor();
      if (! Modifier.isPrivate(constructor.getModifiers())) {
        throw new AssertionError("Constructor is not private");
      }
      constructor.setAccessible(true);
      constructor.newInstance();
      constructor.setAccessible(false);
      for (Method method : cls.getDeclaredMethods()) {
        if (! Modifier.isStatic(method.getModifiers())) {
          throw new AssertionError("There exists a non-static method: " + method);
        }
      }
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }
}
