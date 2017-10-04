package com.obsidiandynamics.proc;

import com.obsidiandynamics.concat.*;

public final class Bourne implements Shell {
  private String path;
  
  public enum Variant {
    SH,
    BASH,
    DASH;
    
    String getShell() {
      return name().toLowerCase();
    }
  }
  
  private Variant variant = Variant.BASH;
  
  public String withPath(String path) {
    this.path = path;
    return path;
  }
  
  @Override
  public String[] toArgs(String... commandFrags) {
    return new String[] { variant.getShell(), "-c", parseCommand(commandFrags) };
  }
  
  private String parseCommand(String[] commandFrags) {
    if (path != null) {
      return new Concat()
          .when(path != null).append(new Concat("export PATH=$PATH:")
                                     .append(path)
                                     .append(" && "))
          .appendArray(" ", (Object[]) commandFrags)
          .toString();
    } else {
      return new Concat()
          .appendArray(" ", (Object[]) commandFrags)
          .toString();
    }
  }
}
