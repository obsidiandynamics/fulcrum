package com.obsidiandynamics.shell;

import com.obsidiandynamics.concat.*;

/**
 *  The Bourne shell, including common variants.
 */
public final class BourneShell implements Shell {
  private String path;
  
  public enum Variant {
    SH,
    BASH,
    DASH;
    
    /**
     *  Gets the name of the shell executable.
     *  
     *  @return The shell executable, e.g. {@code sh}.
     */
    String getShellExecutable() {
      return name().toLowerCase();
    }
    
    /**
     *  Determines whether this shell is installed on the current machine.
     *  
     *  @return True if the shell is installed.
     */
    boolean isAvailable() {
      return new DefaultProcessExecutor().canTryRun("sh", "-c", "pwd");
    }
  }
  
  private Variant variant = Variant.SH;
  
  /**
   *  Assigns a path that will be used to execute a subsequent command.
   *  
   *  @param path The path to set.
   *  @return The current {@link BourneShell} instance for chaining.
   */
  public BourneShell withPath(String path) {
    this.path = path;
    return this;
  }
 
  /**
   *  Specifies the variant of the Bourne shell to use.
   *  
   *  @param variant The shell variant.
   *  @return The current {@link BourneShell} instance for chaining.
   */
  public BourneShell withVariant(Variant variant) {
    this.variant = variant;
    return this;
  }
  
  @Override
  public String[] prepare(String... command) {
    return new String[] { variant.getShellExecutable(), "-c", parseCommand(command) };
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
