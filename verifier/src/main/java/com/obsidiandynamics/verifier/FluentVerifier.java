package com.obsidiandynamics.verifier;

import static com.obsidiandynamics.func.Functions.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

import com.obsidiandynamics.func.*;

import nl.jqno.equalsverifier.internal.prefabvalues.*;
import nl.jqno.equalsverifier.internal.prefabvalues.factories.*;
import nl.jqno.equalsverifier.internal.reflection.*;

public final class FluentVerifier {
  private final Class<?> classUnderTest;
  
  private final FactoryCache factoryCache;
  
  private final PrefabValues prefabValues;
  
  private final Set<String> excludedFields = new HashSet<>();
  
  private MethodNameFormat methodNameFormat = MethodNameFormat.Presets.withXxx;

  FluentVerifier(Class<?> classUnderTest) {
    this.classUnderTest = classUnderTest;
    
    final FactoryCache initialCache = new FactoryCache();
    factoryCache = JavaApiPrefabValues.build().merge(initialCache);
    prefabValues = new PrefabValues(factoryCache);
  }
  
  public FluentVerifier withMethodNameFormat(MethodNameFormat methodNameFormat) {
    mustExist(methodNameFormat, "Method name format cannot be null");
    this.methodNameFormat = methodNameFormat;
    return this;
  }
  
  public FluentVerifier excludeField(String fieldName) {
    mustExist(fieldName, "Field name cannot be null");
    ensureFieldExists(classUnderTest, fieldName);
    excludedFields.add(fieldName);
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

  public <T> FluentVerifier withPrefabValues(Class<? super T> type, T red, T black) {
    mustExist(type, "Class type cannot be null");
    mustExist(red, "Value 'red' cannot be null");
    mustExist(black, "Value 'black' cannot be null");

    if (red.equals(black)) {
      throw new IllegalArgumentException("Both 'red' and 'black' values are equal");
    }

    final T redCopy;
    if (red.getClass().isArray()) {
      redCopy = red;
    } else {
      redCopy = ObjectAccessor.of(red).copy();
    }
    
    factoryCache.put(type, new PrefabValueFactory<T>() {
      @Override
      public Tuple<T> createValues(TypeTag tag, PrefabValues prefabValues, LinkedHashSet<TypeTag> typeStack) {
        return new Tuple<T>(red, black, redCopy);
      }
    });
    return this;
  }

  public static FluentVerifier forClass(Class<?> classUnderTest) {
    mustExist(classUnderTest, "Class under test cannot be null");
    return new FluentVerifier(classUnderTest);
  }

  static final class NoFluentMutatorError extends AssertionError {
    private static final long serialVersionUID = 1L;
    NoFluentMutatorError(String m) { super(m); }
  }

  static final class ReflectionError extends AssertionError {
    private static final long serialVersionUID = 1L;
    ReflectionError(Throwable cause) { super(cause); }
  }

  static final class MethodNotChainableError extends AssertionError {
    private static final long serialVersionUID = 1L;
    MethodNotChainableError(String m) { super(m); }
  }

  static final class IneffectualAssignmentError extends AssertionError {
    private static final long serialVersionUID = 1L;
    IneffectualAssignmentError(String m) { super(m); }
  }

  static final class IllegalMethodModifierError extends AssertionError {
    private static final long serialVersionUID = 1L;
    IllegalMethodModifierError(String m) { super(m); }
  }
  
  static final class NoFieldsError extends AssertionError {
    private static final long serialVersionUID = 1L;
    NoFieldsError(String m) { super(m); }
  } 

  public void verify() {
    final List<Field> fields = getFields(classUnderTest, excludedFields);
    if (fields.isEmpty()) throw new NoFieldsError("No eligible fields to verify");
    
    final Object instance = prefabValues.giveRed(new TypeTag(classUnderTest));
    final TypeTag instanceType = new TypeTag(classUnderTest);
    for (Field field : fields) {
      field.setAccessible(true);
      final Object currentFieldValue = Exceptions.wrap(() -> field.get(instance), ReflectionError::new);
      final TypeTag fieldTypeTag = TypeTag.of(field, instanceType);
      final String methodName = methodNameFormat.getMethodName(field.getName());
      final Method method = Exceptions.wrap(() -> getMethodOrNull(classUnderTest, methodName, field.getType()), 
                                            ReflectionError::new);
      if (method == null) {
        throw new NoFluentMutatorError("No fluent mutator " + methodName + " for field " + field);
      }
      method.setAccessible(true);

      final int methodModifiers = method.getModifiers();
      if (Modifier.isPrivate(methodModifiers) || Modifier.isAbstract(methodModifiers) || Modifier.isStatic(methodModifiers)) {
        throw new IllegalMethodModifierError("The method " + methodName + " may not be private, abstract or static");
      }

      final Object alternateFieldValue = prefabValues.giveOther(fieldTypeTag, currentFieldValue);
      final Object returnValue = Exceptions.wrap(() -> method.invoke(instance, alternateFieldValue), ReflectionError::new);
      if (returnValue != instance) {
        throw new MethodNotChainableError("Return from method " + methodName + " did not match the enclosing instance");
      }

      final Object newFieldValue = Exceptions.wrap(() -> field.get(instance), ReflectionError::new);
      if (! Objects.deepEquals(alternateFieldValue, newFieldValue)) {
        throw new IneffectualAssignmentError("Invoking method " + methodName + " had no effect on the field " + field);
      }
    }
  }

  private static Method getMethodOrNull(Class<?> cls, String methodName, Class<?> paramType) throws SecurityException {
    try {
      return cls.getDeclaredMethod(methodName, paramType);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  private static List<Field> getFields(Class<?> cls, Collection<String> exclusions) {
    return Arrays.stream(cls.getDeclaredFields())
        .filter(FluentVerifier::fieldFilter)
        .filter(field -> ! exclusions.contains(field.getName()))
        .collect(Collectors.toList());
  }

  private static boolean fieldFilter(Field field) {
    return ! Modifier.isFinal(field.getModifiers()) && ! field.isSynthetic();
  }
}
