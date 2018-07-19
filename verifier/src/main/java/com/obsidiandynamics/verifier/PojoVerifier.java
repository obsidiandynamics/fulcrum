package com.obsidiandynamics.verifier;

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
  
  private boolean allAccessorsExcluded;
  
  private boolean allMutatorsExcluded;
  
  private boolean allToStringFieldsExcluded;
  
  private boolean constructorExcluded;
  
  private PojoVerifier(Class<?> classUnderTest) {
    this.classUnderTest = classUnderTest;
  }

  public static PojoVerifier forClass(Class<?> classUnderTest) {
    return new PojoVerifier(classUnderTest);
  }
  
  public PojoVerifier excludeAccessor(String fieldName) {
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
      Assertions.assertPojoMethodsFor(classUnderTest, FieldPredicate.exclude(new ArrayList<>(excludedAccessors)))
      .testing(Method.GETTER)
      .areWellImplemented();
    }
    
    if (! allMutatorsExcluded) {
      Assertions.assertPojoMethodsFor(classUnderTest, FieldPredicate.exclude(new ArrayList<>(excludedMutators)))
      .testing(Method.SETTER)
      .areWellImplemented(); 
    }
    
    if (! allToStringFieldsExcluded) {
      Assertions.assertPojoMethodsFor(classUnderTest, FieldPredicate.exclude(new ArrayList<>(excludedToStringFields)))
      .testing(Method.TO_STRING)
      .areWellImplemented(); 
    }
    
    if (! constructorExcluded) {
      Assertions.assertPojoMethodsFor(classUnderTest)
      .testing(Method.CONSTRUCTOR)
      .areWellImplemented();
    }
  }
}