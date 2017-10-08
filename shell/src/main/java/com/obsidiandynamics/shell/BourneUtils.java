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
