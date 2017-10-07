package com.obsidiandynamics.assertion;

import java.lang.reflect.*;
import java.util.*;

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
  
  static Runnable asserterOfTrue(boolean b) {
    return () -> {
      assert b;
    };
  }
  
  static boolean not(boolean b) {
    return ! b;
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
      throw new AssertionError("The toString() method does not appear to have been overriden");
    }
  }

  /**
   *  Verifies that a utility class is well defined.
   *
   *  Adapted from
   *  https://github.com/trajano/maven-jee6/blob/master/maven-jee6-test/src/test/java/net/trajano/maven_jee6/test/test/UtilityClassTestUtilTest.java
   *
   *  @param cls Utility class to verify.
   *  @throws Exception If an error occurred.
   */
  public static void assertUtilityClassWellDefined(Class<?> cls) throws Exception {
    if (! Modifier.isFinal(cls.getModifiers())) {
      throw new AssertionError("Class must be final");
    }
    
    if (cls.getDeclaredConstructors().length != 1) {
      throw new AssertionError("There must be exactly one construtor");
    }
    
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
  }
}
