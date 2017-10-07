package com.obsidiandynamics.dockercompose;

import java.util.concurrent.atomic.*;

import com.obsidiandynamics.concat.*;
import com.obsidiandynamics.shell.*;

/**
 *  Wrapper around the {@code docker-compose} CLI.
 */
public final class DockerCompose {
  private String composeFile;
  
  private String project;
  
  private Shell shell = Shell.getDefault();
  
  private ProcessExecutor executor = ProcessExecutor.getDefault();

  public DockerCompose withComposeFile(String composeFile) {
    this.composeFile = composeFile;
    return this;
  }

  public DockerCompose withProject(String project) {
    this.project = project;
    return this;
  }

  public DockerCompose withShell(Shell shell) {
    this.shell = shell;
    return this;
  }

  public DockerCompose withExecutor(ProcessExecutor executor) {
    this.executor = executor;
    return this;
  }
  
  public static final class NotInstalledError extends AssertionError {
    private static final long serialVersionUID = 1L;
    NotInstalledError(String m) { super(m); }
  }
  
  /**
   *  Checks whether the {@code docker-compose} CLI is installed. Behind the scenes, this
   *  method tries to run {@code docker-compose version}, which should return a zero exit code if
   *  it is correctly installed. Failing this will throw a {@link NotInstalledError} - a
   *  subclass of {@link AssertionError}.
   *  
   *  @exception NotInstalledError If {@code docker-compose} isn't properly installed.
   */
  public void checkInstalled() {
    final int exitCode = Shell.builder()
        .withShell(shell)
        .withExecutor(executor)
        .execute("docker-compose version")
        .await();
    if (exitCode != 0) {
      throw new NotInstalledError("docker-compose is not correctly installed");
    }
  }
  
  private void ensureComposeFileAssigned() {
    if (composeFile == null) throw new IllegalStateException("Compose file must be assigned");
  }
  
  public void up() throws DockerComposeException {
    ensureComposeFileAssigned();
    final Concat cmd = new Concat("docker-compose")
        .whenIsNotNull(project).append(new StringBuilder(" -p ").append(project))
        .append(" -f ").append(composeFile)
        .append(" up -d");
    run(cmd.toString());
  }
  
  public void down(boolean removeVolumes) throws DockerComposeException {
    ensureComposeFileAssigned();
    final Concat cmd = new Concat("docker-compose")
        .whenIsNotNull(project).append(new StringBuilder(" -p ").append(project))
        .append(" -f ").append(composeFile)
        .append(" down")
        .when(removeVolumes).append(" -v");
    run(cmd.toString());
  }
  
  public void stop(int timeout) throws DockerComposeException {
    ensureComposeFileAssigned();
    final Concat cmd = new Concat("docker-compose")
        .whenIsNotNull(project).append(new StringBuilder(" -p ").append(project))
        .append(" -f ").append(composeFile)
        .append(" stop")
        .when(timeout != 0).append(new StringBuilder(" -t ").append(timeout));
    run(cmd.toString());
  }
  
  public void rm(boolean removeVolumes) throws DockerComposeException {
    ensureComposeFileAssigned();
    final Concat cmd = new Concat("docker-compose")
        .whenIsNotNull(project).append(new StringBuilder(" -p ").append(project))
        .append(" -f ").append(composeFile)
        .append(" rm -f")
        .when(removeVolumes).append(" -v");
    run(cmd.toString());
  }
  
  private void run(String command) throws DockerComposeException {
    final AtomicReference<String> sink = new AtomicReference<>();
    final int exitCode = Shell.builder()
        .withShell(shell)
        .withExecutor(executor)
        .execute(command)
        .pipeTo(sink::set)
        .await();
    if (exitCode != 0) {
      throw new DockerComposeException(command, sink.get(), exitCode);
    }
  }
}
