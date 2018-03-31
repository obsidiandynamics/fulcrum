package com.obsidiandynamics.classes;

public final class Classes {
  private Classes() {}
  
  @SuppressWarnings("unchecked")
  public static <T> T cast(Object obj) {
    return (T) obj;
  }
  
  public static String compressPackage(String className, int depth) {
    final String[] frags = className.split("\\.");
    final StringBuilder formatted = new StringBuilder();
    for (int i = 0; i < frags.length; i++) {
      if (formatted.length() != 0) formatted.append('.');
      if (i < depth && i < frags.length - 1) {
        formatted.append(frags[i].charAt(0));
      } else {
        formatted.append(frags[i]);
      }
    }
    return formatted.toString();
  }
}
