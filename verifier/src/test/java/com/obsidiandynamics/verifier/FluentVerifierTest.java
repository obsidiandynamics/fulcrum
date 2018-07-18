package com.obsidiandynamics.verifier;

import java.util.*;

import org.junit.*;

import com.obsidiandynamics.verifier.FluentVerifier.*;

public final class FluentVerifierTest {
  static final class PassFluent {
    final int a;
    
    int b;
    
    String cx;
    
    int[] array;
    
    List<UUID> list;
    
    PassFluent(int a) { this.a = a; }

    public PassFluent withB(int b) {
      this.b = b;
      return this;
    }

    PassFluent withCx(String cx) {
      this.cx = cx;
      return this;
    }
    
    PassFluent withArray(int[] array) {
      this.array = array;
      return this;
    }
    
    PassFluent withList(List<UUID> list) {
      this.list = list;
      return this;
    }
  }
  
  @Test
  public void testPass() {
    FluentVerifier.forClass(PassFluent.class).verify();
  }
  
  static final class NoMutatorMethod {
    int a;
    int b;
    
    NoMutatorMethod withA(int a) {
      this.a = a;
      return this;
    }
  }

  @Test(expected=NoFluentMutatorError.class)
  public void testFailNoMutatorMethod() {
    FluentVerifier.forClass(NoMutatorMethod.class).verify();
  }
  
  @Test
  public void testExcludeField() {
    FluentVerifier.forClass(NoMutatorMethod.class).excludeField("b").verify();
  }
  
  static final class ReturnsVoid {
    int b;
    
    void withB(int b) {
      this.b = b;
    }
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testExcludeInvalidField() {
    FluentVerifier.forClass(NoMutatorMethod.class).excludeField("c").verify();
  }

  @Test(expected=MethodNotChainableError.class)
  public void testFailMethodNotChainable() {
    FluentVerifier.forClass(ReturnsVoid.class).verify();
  }
  
  static final class NoAssignmentEffect {
    int b;
    
    NoAssignmentEffect withB(int b) {
      return this;
    }
  }

  @Test(expected=IneffectualAssignmentError.class)
  public void testFailIneffectualAssignment() {
    FluentVerifier.forClass(NoAssignmentEffect.class).verify();
  }
  
  static final class Recurse {
    int x;
    
    Recurse next;
    
    Recurse(int x, Recurse next) {
      this.x = x;
      this.next = next;
    }
    
    Recurse withX(int x) {
      this.x = x;
      return this;
    }

    Recurse withNext(Recurse next) {
      this.next = next;
      return this;
    }
  }
  
  @Test
  public void testRecurse() {
    FluentVerifier
    .forClass(Recurse.class)
    .withPrefabValues(Recurse.class, new Recurse(0, null), new Recurse(1, null))
    .verify();
  }
  
  static final class PrivateMethod {
    int b;
    
    PrivateMethod(int b) {
      withB(b);
    }
    
    private PrivateMethod withB(int b) {
      return this;
    }
  }

  @Test(expected=IllegalMethodModifierError.class)
  public void testIllegalModifierPrivateMethod() {
    FluentVerifier.forClass(PrivateMethod.class).verify();
  }
  
  static abstract class AbstractMethod {
    int b;
    
    abstract AbstractMethod withB(int b);
  }

  @Test(expected=IllegalMethodModifierError.class)
  public void testIllegalModifierAbstractMethod() {
    FluentVerifier.forClass(AbstractMethod.class).verify();
  }
  
  static final class StaticMethod {
    int b;
    
    static StaticMethod withB(int b) {
      return new StaticMethod();
    }
  }

  @Test(expected=IllegalMethodModifierError.class)
  public void testIllegalModifierStaticMethod() {
    FluentVerifier.forClass(StaticMethod.class).verify();
  }
  
  static final class ExceptionInMethod {
    int b;
    
    ExceptionInMethod(int b) {
      withB(b);
    }
    
    ExceptionInMethod withB(int b) {
      throw new RuntimeException("Faulty method");
    }
  }

  @Test(expected=ReflectionError.class)
  public void testReflectionError() {
    FluentVerifier.forClass(ExceptionInMethod.class).verify();
  }
  
  @Test(expected=NoFieldsError.class)
  public void testNoFieldsError() {
    FluentVerifier.forClass(Object.class).verify();
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testPrefabRedBlackEquals() {
    FluentVerifier.forClass(Object.class).withPrefabValues(String.class, "s", "s");
  }
  
  @Test
  public void testPrefabArray() {
    FluentVerifier.forClass(Object.class).withPrefabValues(int[].class, new int[] {0}, new int[] {1});
  }
  
  static final class SetMethod {
    int field;
    
    SetMethod setField(int field) {
      this.field = field;
      return this;
    }
  }
  
  @Test
  public void testWithFormat() {
    FluentVerifier.forClass(SetMethod.class).withMethodNameFormat(MethodNameFormat.Presets.setXxx).verify();
  }
}
