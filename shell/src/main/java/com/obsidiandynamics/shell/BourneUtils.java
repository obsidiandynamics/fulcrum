package com.obsidiandynamics.shell;

import static com.obsidiandynamics.shell.BourneShell.Variant.*;

import java.util.function.*;

/**
 *  Utilities specific to the Bourne shell.
 */
public final class BourneUtils {
  private BourneUtils() {}
  
//  public static void checkInstalled(String name, String verifyCmd) {
//    final StringBuilder out = new StringBuilder();
//    final int code = BashInteractor.execute(commandWithPath(verifyCmd), true, out::append);
//    if (code == 127) {
//      final AtomicReference<String> path = new AtomicReference<>();
//      BashInteractor.execute(commandWithPath("echo $PATH"), true, path::set);
//      throw new AssertionError(name + " is not installed, or is missing from the path (" + path + ")");
//    } else if (code != 0) {
//      throw new AssertionError("Error running " + name + ": " + out);
//    }
//  }
  
  /**
   *  Pre-canned {@code sh} command executor, printing the command to the sink as
   *  the first line, followed by the output of the command in the subsequent lines.
   *  
   *  @param command The command to execute in {@code sh}.
   *  @param sink The output sink.
   *  @return The exit code, or {@code -1} if this method was interrupted.
   */
  public static int run(String command, Consumer<String> sink) {
    final StringBuilder buf = new StringBuilder();
    buf.append("$ ").append(command).append('\n');
    final int exitCode = Shell.builder()
    .withShell(new BourneShell().withVariant(SH))
    .execute(command)
    .pipeTo(buf::append)
    .await();
    sink.accept(buf.toString());
    return exitCode;
  }
}
