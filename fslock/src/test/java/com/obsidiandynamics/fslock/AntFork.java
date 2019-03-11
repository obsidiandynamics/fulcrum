package com.obsidiandynamics.fslock;

import static com.obsidiandynamics.func.Functions.*;

import java.io.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.types.Commandline.*;

/**
 *  Forks the given 'main' class in a new JVM process using Ant, pipes the process
 *  outputs to a pair of {@link PrintStream} instances and awaits process
 *  termination. <p>
 *  
 *  It is assumed that the target class and all of its dependencies are already in the
 *  classpath of the parent process; this classpath is reused for the forked JVM. (Further
 *  JVM arguments may be supplied if necessary.)
 */
public final class AntFork {
  public static BuildLogger createDefaultLogger() {
    return createLogger(System.out, System.err, Project.MSG_INFO);
  }
  
  public static BuildLogger createLogger(PrintStream outStream, PrintStream errStream, int level) {
    final DefaultLogger logger = new DefaultLogger();
    logger.setOutputPrintStream(mustExist(outStream));
    logger.setErrorPrintStream(mustExist(errStream));
    logger.setMessageOutputLevel(level);
    return logger;
  }

  private final Class<?> mainClass;

  private BuildLogger logger = createDefaultLogger();

  private String jvmArgs = "";

  public AntFork(Class<?> mainClass) {
    this.mainClass = mustExist(mainClass);
  }

  public AntFork withLogger(BuildLogger logger) {
    this.logger = mustExist(logger);
    return this;
  }

  public AntFork withJvmArgs(String jvmArgs) {
    this.jvmArgs = mustExist(jvmArgs);
    return this;
  }

  public int run() {
    final Project project = new Project();
    project.setBaseDir(new File(System.getProperty("user.dir")));
    project.init();
    project.addBuildListener(logger);
    project.fireBuildStarted();

    final Java javaTask = new Java();
    javaTask.setNewenvironment(true);
    javaTask.setTaskName("run");
    javaTask.setProject(project);
    javaTask.setFork(true);
    javaTask.setFailonerror(true);
    javaTask.setClassname(mainClass.getName());

    final Argument jvmArgs = javaTask.createJvmarg();
    jvmArgs.setLine(this.jvmArgs);

    final Path classPath = new Path(project);
    javaTask.setClasspath(classPath);
    classPath.setPath(System.getProperty("java.class.path"));

    javaTask.init();
    return javaTask.executeJava();
  }
}
