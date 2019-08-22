package com.obsidiandynamics.verifier;

final class Verifiers {
  private Verifiers() {}
  
  static boolean isFieldValid(Class<?> cls, String fieldName) throws SecurityException {
    try {
      cls.getDeclaredField(fieldName);
      return true;
    } catch (NoSuchFieldException e) {
      return false;
    }
  }
}
