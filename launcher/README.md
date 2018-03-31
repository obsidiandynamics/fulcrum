fulcrum-launcher
===

Example usage:

Firstly, add the following snippet to your root `build.gradle`:

```groovy
project.ext.set("launcherClasspath", new org.gradle.api.internal.file.UnionFileCollection())
project.ext.set("launcherClasses", new ArrayList())

task launch() {
  dependsOn allprojects*.tasks*.findByName("testClasses")
  doLast {
    def baseJvmArgs = "-XX:-MaxFDLimit -XX:+TieredCompilation -XX:+UseNUMA -XX:+UseCondCardMark " + 
                      "-XX:-UseBiasedLocking -Xms2G -Xmx2G -Xss1M -XX:+UseG1GC -XX:MaxGCPauseMillis=200 " + 
                      "-XX:InitiatingHeapOccupancyPercent=0 -Djava.net.preferIPv4Stack=true " + 
                      "-Dlauncher.package.compress.level=3"
    javaexec {
      systemProperties = System.properties
      classpath = project(":").launcherClasspath
      main = "com.obsidiandynamics.launcher.Launcher"
      args = project(":").launcherClasses
      jvmArgs = Arrays.asList baseJvmArgs.split(" ")
      standardInput = System.in
    }
  }
}
```

Replace `baseJvmArgs` as appropriate.

Then, for every module which has launchable apps, add the following to the module's `build.gradle`:

```groovy
task addLauncherApps() {
  project(":").launcherClasspath += sourceSets.main.runtimeClasspath + sourceSets.test.runtimeClasspath
  project(":").launcherClasses += "com.obsidiandynamics.foo.FooTest1"
  project(":").launcherClasses += "com.obsidiandynamics.foo.FooTest2"
  project(":").launcherClasses += "com.obsidiandynamics.foo.FooTest3"
}
```

Replace the values for `launcherClasses` as appropriate.