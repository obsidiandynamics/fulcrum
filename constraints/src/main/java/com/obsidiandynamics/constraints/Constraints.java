package com.obsidiandynamics.constraints;

import java.util.*;

import javax.validation.*;

/**
 *  Helper for working with {@code javax.validation}.
 */
public final class Constraints {
  /**
   *  The holder class defers initialisation until needed.
   */
  private static class Holder {
    static final Validator VALIDATOR;

    static {
      try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
        VALIDATOR = factory.getValidator();
      }
    }
  }
  
  private Constraints() {}
  
  /**
   *  Validates the given object, returning the object if successful, or throwing a
   *  {@link ConstraintViolationException} if validation fails.
   *  
   *  @param <T> Object type.
   *  @param obj The object to validate.
   *  @return The object, if it passes validation.
   *  @throws ConstraintViolationException If validation fails.
   */
  public static <T> T validate(T obj) {
    final Set<ConstraintViolation<T>> violations = Holder.VALIDATOR.validate(obj);
    if (! violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    } else {
      return obj;
    }
  }
}
