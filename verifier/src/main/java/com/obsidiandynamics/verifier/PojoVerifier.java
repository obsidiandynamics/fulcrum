package com.obsidiandynamics.verifier;

import static com.obsidiandynamics.func.Functions.*;

import java.lang.reflect.*;
import java.util.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.verifier.FluentVerifier.*;

import pl.pojo.tester.api.*;
import pl.pojo.tester.api.assertion.*;
import pl.pojo.tester.api.assertion.Method;

public final class PojoVerifier {
  private final Class<?> classUnderTest;
  
  private final Set<String> excludedAccessors = new HashSet<>();
  
  private final Set<String> excludedMutators = new HashSet<>();
  
  private final Set<String> excludedToStringFields = new HashSet<>();
  
  private final Map<Class<?>, ConstructorArgs> constructorArgsForType = new HashMap<>();
  
  private boolean allAccessorsExcluded;
  
  private boolean allMutatorsExcluded;
  
  private boolean allToStringFieldsExcluded;
  
  private boolean constructorExcluded;
  
  private PojoVerifier(Class<?> classUnderTest) {
    this.classUnderTest = classUnderTest;
  }

  public static PojoVerifier forClass(Class<?> classUnderTest) {
    mustExist(classUnderTest, "Class under test cannot be null");
    return new PojoVerifier(classUnderTest);
  }
  
  public PojoVerifier constructorArgs(ConstructorArgs constructorArgs) {
    return constructorArgs(classUnderTest, constructorArgs);
  }
  
  public PojoVerifier constructorArgs(Class<?> type, ConstructorArgs constructorArgs) {
    mustExist(type, "Type cannot be null");
    mustExist(constructorArgs, "Constructor args cannot be null");
    constructorArgsForType.put(type, constructorArgs);
    return this;
  }
  
  public PojoVerifier excludeAccessor(String fieldName) {
    mustExist(fieldName, "Field name cannot be null");
    ensureFieldExists(classUnderTest, fieldName);
    excludedAccessors.add(fieldName);
    return this;
  }
  
  public PojoVerifier excludeAccessors() {
    allAccessorsExcluded = true;
    return this;
  }
  
  Set<String> getExcludedAccessors() {
    return excludedAccessors;
  }
  
  public PojoVerifier excludeMutator(String fieldName) {
    mustExist(fieldName, "Field name cannot be null");
    ensureFieldExists(classUnderTest, fieldName);
    excludedMutators.add(fieldName);
    return this;
  }
  
  public PojoVerifier excludeMutators() {
    allMutatorsExcluded = true;
    return this;
  }
  
  Set<String> getExcludedMutators() {
    return excludedMutators;
  }
  
  private void autoExcludeFinalFields() {
    for (Field field : classUnderTest.getDeclaredFields()) {
      if (Modifier.isFinal(field.getModifiers())) {
        excludeMutator(field.getName());
      }
    }
  }
  
  public PojoVerifier excludeToStringField(String fieldName) {
    mustExist(fieldName, "Field name cannot be null");
    ensureFieldExists(classUnderTest, fieldName);
    excludedToStringFields.add(fieldName);
    return this;
  }
  
  private static void ensureFieldExists(Class<?> cls, String fieldName) {
    if (! Exceptions.wrap(() -> isFieldValid(cls, fieldName), ReflectionError::new)) {
      throw new IllegalArgumentException("Invalid field " + fieldName);
    }
  }
  
  private static boolean isFieldValid(Class<?> cls, String fieldName) throws SecurityException {
    try {
      cls.getDeclaredField(fieldName);
      return true;
    } catch (NoSuchFieldException e) {
      return false;
    }
  }
  
  public PojoVerifier excludeToStringFields() {
    allToStringFieldsExcluded = true;
    return this;
  }
  
  public PojoVerifier excludeConstructor() {
    constructorExcluded = true;
    return this;
  }
  
  public void verify() {
    autoExcludeFinalFields();
    
    if (! allAccessorsExcluded) {
      final AbstractAssertion a = Assertions.assertPojoMethodsFor(classUnderTest, FieldPredicate.exclude(new ArrayList<>(excludedAccessors)));
      prepareAssertion(a)
      .testing(Method.GETTER)
      .areWellImplemented();
    }
    
    if (! allMutatorsExcluded) {
      final AbstractAssertion a = Assertions.assertPojoMethodsFor(classUnderTest, FieldPredicate.exclude(new ArrayList<>(excludedMutators)));
      prepareAssertion(a)
      .testing(Method.SETTER)
      .areWellImplemented(); 
    }
    
    if (! allToStringFieldsExcluded) {
      final AbstractAssertion a = Assertions.assertPojoMethodsFor(classUnderTest, FieldPredicate.exclude(new ArrayList<>(excludedToStringFields)));
      prepareAssertion(a)
      .testing(Method.TO_STRING)
      .areWellImplemented(); 
    }
    
    if (! constructorExcluded) {
      final AbstractAssertion a = Assertions.assertPojoMethodsFor(classUnderTest);
      prepareAssertion(a)
      .testing(Method.CONSTRUCTOR)
      .areWellImplemented();
    }
  }
  
  private AbstractAssertion prepareAssertion(AbstractAssertion a) {
    for (Map.Entry<Class<?>, ConstructorArgs> entry : constructorArgsForType.entrySet()) {
      a.create(entry.getKey(), entry.getValue().toConstructorParameters());
    }
    return a;
  }
}
