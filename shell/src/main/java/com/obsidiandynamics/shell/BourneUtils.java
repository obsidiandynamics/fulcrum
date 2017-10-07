package com.obsidiandynamics.shell;

import static com.obsidiandynamics.shell.BourneShell.Variant.*;

/**
 *  Utilities specific to the Bourne shell.
 */
public final class BourneUtils {
  private BourneUtils() {}
  
  /**
   *  Determines whether {@code sh} is installed on this machine.
   *  
   *  @return True if {@code sh} is installed.
   */
  public static boolean isShellAvailable() {
    return BourneShell.Variant.SH.isAvailable();
  }
  
  /**
   *  Determines if the given exit code represents a success status.
   *  
   *  @param exitCode The exit code to test.
   *  @return True if this code represents a success status.
   */
  public static boolean isSuccess(int exitCode) {
    return exitCode == 0;
  }
  
  /**
   *  Determines if the given exit code implies that a command wasn't found or wasn't
   *  an executable.
   *  
   *  @param exitCode The exit code to test.
   *  @return True if this code represents a not-found/non-executable status.
   */
  public static boolean isNotFoundOrNotExecutable(int exitCode) {
    return exitCode == 126 || exitCode == 127;
  }
  
  /**
   *  Pre-canned {@code sh} command executor, piping the output to the
   *  specified {@code sink}.
   *  
   *  @param command The command to execute in {@code sh}.
   *  @param path The optional additional path (may be left {@code null}).
   *  @param echo Whether the command should be echoed to the sink.
   *  @param sink The output sink.
   *  @return The exit code, or {@code -1} if this method was interrupted.
   */
  public static int run(String command, String path, boolean echo, Sink sink) {
    final RunningProcess proc = Shell.builder()
        .withShell(new BourneShell().withVariant(SH).withPath(path))
        .execute(command);
    
    if (echo) {
      proc.echo(sink);
    }
    
    return proc.pipeTo(sink)
        .await();
  }
}
