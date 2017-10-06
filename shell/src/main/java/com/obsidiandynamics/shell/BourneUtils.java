package com.obsidiandynamics.shell;

import static com.obsidiandynamics.shell.BourneShell.Variant.*;

import java.util.concurrent.atomic.*;
import java.util.function.*;

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
   *  Determines whether a specific application is installed, which is deemed true if the
   *  given {@code verifyCommand} returns a zero exit code, and false if the exit code
   *  is {@code 127}. Otherwise, a {@code CommandExecutionException} is thrown.
   *  
   *  @param verifyCommand The verify command.
   *  @param path The optional additional path (may be left {@code null}).
   *  @return True if the command is installed.
   *  @throws CommandExecutionException If running the command generated an error.
   */
  public static boolean isInstalled(String verifyCommand, String path) {
    final StringBuilder out = new StringBuilder();
    final int code = run(verifyCommand, path, out::append);
    if (isSuccess(code)) {
      return true;
    } else if (isNotFoundOrNotExecutable(code)) {
      return false;
    } else {
      throw new CommandExecutionException("Error running " + verifyCommand + ": " + out, null);
    }
  }
  
  public static final class NotInstalledError extends AssertionError {
    private static final long serialVersionUID = 1L;
    NotInstalledError(String m) { super(m); }
  }
  
  /**
   *  Asserts whether a given command is installed. Internally, this delegates to
   *  {@link BourneUtils#isInstalled(String, String)}, throwing a {@link NotInstalledError} - a
   *  subclass of {@link AssertionError} if the command failed to run.
   *  
   *  @param verifyCommand The verify command.
   *  @param path The optional additional path (may be left {@code null}).
   *  @param name The name of the program; will appear in the exception message.
   *  @exception NotInstalledError If the command is not installed.
   *  @exception CommandExecutionException If running the command generated an error.
   */
  public static void assertInstalled(String verifyCommand, String path, String name) throws NotInstalledError, CommandExecutionException {
    if (! isInstalled(path, verifyCommand)) {
      final AtomicReference<String> completePath = new AtomicReference<>();
      run("echo $PATH", path, completePath::set);
      throw new NotInstalledError(name + " is not installed, or is missing from the path (" + completePath + ")");
    }
  }
  
  /**
   *  Pre-canned {@code sh} command executor, printing the command to the sink as
   *  the first line, followed by the output of the command in the subsequent lines.
   *  
   *  @param command The command to execute in {@code sh}.
   *  @param path The optional additional path (may be left {@code null}).
   *  @param sink The output sink.
   *  @return The exit code, or {@code -1} if this method was interrupted.
   */
  public static int runVerbose(String command, String path, Consumer<String> sink) {
    final StringBuilder buf = new StringBuilder();
    buf.append("$ ").append(command).append('\n');
    final int exitCode = run(command, path, buf::append);
    sink.accept(buf.toString());
    return exitCode;
  }
  
  /**
   *  Pre-canned {@code sh} command executor, piping the output to the
   *  specified {@code sink}.
   *  
   *  @param command The command to execute in {@code sh}.
   *  @param path The optional additional path (may be left {@code null}).
   *  @param sink The output sink.
   *  @return The exit code, or {@code -1} if this method was interrupted.
   */
  public static int run(String command, String path, Consumer<String> sink) {
    return Shell.builder()
    .withShell(new BourneShell().withVariant(SH).withPath(path))
    .execute(command)
    .pipeTo(sink)
    .await();
  }
}
