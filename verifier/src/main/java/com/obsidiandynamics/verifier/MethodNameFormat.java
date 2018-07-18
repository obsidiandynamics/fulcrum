package com.obsidiandynamics.verifier;

import static java.lang.Character.*;

@FunctionalInterface
public interface MethodNameFormat {
  String getMethodName(String fieldName);
  
  default MethodNameFormat then(MethodNameFormat next) {
    return fieldName -> next.getMethodName(getMethodName(fieldName));
  }

  final class Presets {
    private Presets() {}
    
    public static final MethodNameFormat withXxx = addPrefix("with");
    
    public static final MethodNameFormat setXxx = addPrefix("set");

    public static final MethodNameFormat xxx = fieldName -> fieldName;
  }
  
  static MethodNameFormat addPrefix(String prefix) {
    return fieldName -> prefix + toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
  }
  
  static MethodNameFormat stripSuffix(String suffix) {
    return fieldName -> {
      if (fieldName.endsWith(suffix)) {
        return fieldName.substring(0, fieldName.length() - suffix.length());
      } else {
        return fieldName;
      }
    };
  }
}
