package com.obsidiandynamics.shell;

import com.obsidiandynamics.concat.*;

public final class BourneShell implements Shell {
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
  
  public BourneShell withPath(String path) {
    this.path = path;
    return this;
  }
  
  public BourneShell withVariant(Variant variant) {
    this.variant = variant;
    return this;
  }
  
  @Override
  public String[] prepare(String... command) {
    return new String[] { variant.getShell(), "-c", parseCommand(command) };
  }
  
  private String parseCommand(String[] command) {
    return new Concat()
        .when(path != null).append(new Concat("export PATH=$PATH:")
                                   .append(path)
                                   .append(" && "))
        .appendArray(" ", (Object[]) command)
        .toString();
  }
}
