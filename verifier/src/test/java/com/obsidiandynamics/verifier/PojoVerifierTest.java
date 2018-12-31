package com.obsidiandynamics.verifier;

import static com.obsidiandynamics.func.Functions.*;
import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

public final class PojoVerifierTest {
  public static final class TestPojoPartialMethods {
    private final int a;
    private int b;
    private int c;
    
    public TestPojoPartialMethods(int a, int b, int c) {
      this.a = a;
      this.b = b;
      this.c = c;
    }
    
    public void setB(int b) {
      this.b = b;
    }
    
    public int getA() {
      return a;
    }
    
    public int getC() {
      return c;
    }

    @Override
    public String toString() {
      return TestPojoPartialMethods.class.getSimpleName() + " [a=" + a + ", b=" + b + ", c=" + c + "]";
    }
  }
  
  @Test
  public void testAccessorsAndMutators() {
    final PojoVerifier pojoVerifier = PojoVerifier.forClass(TestPojoPartialMethods.class);
    assertNotNull(pojoVerifier);
    assertEquals(Collections.emptySet(), pojoVerifier.getExcludedAccessors());
    assertEquals(Collections.emptySet(), pojoVerifier.getExcludedMutators());
    
    pojoVerifier.excludeAccessor("b");
    assertEquals(Collections.singleton("b"), pojoVerifier.getExcludedAccessors());
    
    pojoVerifier.excludeMutator("c");
    assertEquals(Collections.singleton("c"), pojoVerifier.getExcludedMutators());
    pojoVerifier.verify();
    
    // verify that the final field was excluded
    assertEquals(new HashSet<>(asList("c", "a")), pojoVerifier.getExcludedMutators());
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testInvalidAccessor() {
    PojoVerifier.forClass(TestPojoPartialMethods.class).excludeAccessor("nonexistent");
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testInvalidMutator() {
    PojoVerifier.forClass(TestPojoPartialMethods.class).excludeMutator("nonexistent");
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testInvalidToStringField() {
    PojoVerifier.forClass(TestPojoPartialMethods.class).excludeToStringField("nonexistent");
  }
  
  @Test
  public void testExcludeAccessorsAndMutators() {
    PojoVerifier.forClass(TestPojoPartialMethods.class).excludeAccessors().excludeMutators().verify();
  }
  
  public static final class TestPojoPartialToString {
    final int a;
    final int b;
    
    public TestPojoPartialToString(int a, int b) {
      this.a = a;
      this.b = b;
    }
    
    @Override
    public String toString() {
      return TestPojoPartialToString.class.getSimpleName() + " [a=" + a + "]";
    }
  }
  
  @Test
  public void testExcludeToStringFields() {
    PojoVerifier.forClass(TestPojoPartialToString.class)
    .excludeAccessors()
    .excludeToStringField("b")
    .verify();
  }
  
  @Test
  public void testExcludeAllToStringFields() {
    PojoVerifier.forClass(TestPojoPartialToString.class)
    .excludeAccessors()
    .excludeToStringFields()
    .verify();
  }
  
  public static final class TestPojoBadConstructor {
    public TestPojoBadConstructor() {
      throw new RuntimeException("Faulty constructor");
    }
  }
  
  @Test
  public void testExcludeConstructorAndOthers() {
    PojoVerifier.forClass(TestPojoBadConstructor.class)
    .excludeAccessors()
    .excludeMutators()
    .excludeToStringFields()
    .excludeConstructor().verify();
  }
  
  public static final class Restricted {
    final int value;
    
    Restricted(int value) {
      this.value = mustBeGreater(value, 10, IllegalArgumentException::new);
    }
  }
  
  public static final class TestCustomConstructor {
    final String a;
    final int b;
    final Restricted restricted;
    
    TestCustomConstructor(Restricted restricted) {
      this("foo", 1, restricted);
    }
    
    TestCustomConstructor(String a, int b, Restricted restricted) {
      mustBeTrue(a.length() > 2, IllegalArgumentException::new);
      mustBeGreater(b, 0, IllegalArgumentException::new);
      this.a = a;
      this.b = b;
      this.restricted = restricted;
    }
    
    public String getA() {
      return a;
    }

    public int getB() {
      return b;
    }
    
    public Restricted getRestricted() {
      return restricted;
    }

    @Override
    public String toString() {
      return TestPojoPartialToString.class.getSimpleName() + " [a=" + a + ", b=" + b + ", restricted=" + restricted + "]";
    }
  }
  
  @Test
  public void testCustomConstructorAndOthers() {
    PojoVerifier.forClass(TestCustomConstructor.class)
    .constructorArgs(new ConstructorArgs()
                     .with(String.class, "foo")
                     .with(int.class, 1)
                     .with(Restricted.class, new Restricted(11)))
    .constructorArgs(Restricted.class,
                     new ConstructorArgs()
                     .with(int.class, 11))
    .verify();
  }
}
