
<img src="https://raw.githubusercontent.com/wiki/obsidiandynamics/fulcrum/images/fulcrum-logo.png" width="90px" alt="logo"/> Fulcrum
===
[ ![Download](https://api.bintray.com/packages/obsidiandynamics/fulcrum/fulcrum-shell/images/download.svg) ](https://bintray.com/obsidiandynamics/fulcrum/fulcrum-shell/_latestVersion)

Fulcrum is home to tiny **micro libraries** that do very specific things, and can be imported into a project individually, without dragging in tons of unneeded dependencies.-

# Modules
* [`fulcrum-assert`](https://github.com/obsidiandynamics/fulcrum/tree/master/assert) - Common assertions
* [`fulcrum-concat`](https://github.com/obsidiandynamics/fulcrum/tree/master/concat) - Fluid API for selective concatenation of strings
* [`fulcrum-docker-compose`](https://github.com/obsidiandynamics/fulcrum/tree/master/docker-compose) - Wrapper around the `docker-compose` CLI
* [`fulcrum-junit`](https://github.com/obsidiandynamics/fulcrum/tree/master/junit) - JUnit utilities
* [`fulcrum-shell`](https://github.com/obsidiandynamics/fulcrum/tree/master/shell) - Assists in the execution of shells and processes

# Getting started
Select just the module(s) that you need from the list above. Each module has a short README describing its use.

The builds are hosted on JCenter. (Maven users may need to add the JCenter repository to their POM.) Simply add the following snippet to your build file. Replace the version number in the snippet with the version shown on the Download badge at the top of this README, and `fulcrum-module` with the name of the actual module you need.

For Maven:

```xml
<dependency>
  <groupId>com.obsidiandynamics.fulcrum</groupId>
  <artifactId>fulcrum-module</artifactId>
  <version>0.2.0</version>
  <type>pom</type>
</dependency>
```

For Gradle:

```groovy
compile 'com.obsidiandynamics.fulcrum:fulcrum-module:0.2.0'
```
