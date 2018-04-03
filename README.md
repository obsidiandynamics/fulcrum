
<img src="https://raw.githubusercontent.com/wiki/obsidiandynamics/fulcrum/images/fulcrum-logo.png" width="90px" alt="logo"/> Fulcrum
===
[ ![Download](https://api.bintray.com/packages/obsidiandynamics/fulcrum/fulcrum-shell/images/download.svg) ](https://bintray.com/obsidiandynamics/fulcrum/fulcrum-shell/_latestVersion)
[ ![Build](https://travis-ci.org/obsidiandynamics/fulcrum.svg?branch=master) ](https://travis-ci.org/obsidiandynamics/fulcrum#)
[![codecov](https://codecov.io/gh/obsidiandynamics/fulcrum/branch/master/graph/badge.svg)](https://codecov.io/gh/obsidiandynamics/fulcrum)

Fulcrum is home to tiny **micro libraries** that do very specific things, and can be imported into a project individually, without dragging in tons of unneeded dependencies.

# Modules
* [`fulcrum-assert`](https://github.com/obsidiandynamics/fulcrum/tree/master/assert) — Common assertions
* [`fulcrum-await`](https://github.com/obsidiandynamics/fulcrum/tree/master/await) — Utility for awaiting asynchronous actions
* [`fulcrum-classes`](https://github.com/obsidiandynamics/fulcrum/tree/master/classes) — Utilities for working with objects and classes
* [`fulcrum-concat`](https://github.com/obsidiandynamics/fulcrum/tree/master/concat) — Fluid API for selective concatenation of strings
* [`fulcrum-docker-compose`](https://github.com/obsidiandynamics/fulcrum/tree/master/docker-compose) — Wrapper around the `docker-compose` CLI
* [`fulcrum-flow`](https://github.com/obsidiandynamics/fulcrum/tree/master/flow) — Strictly ordered joining of parallel tasks
* [`fulcrum-func`](https://github.com/obsidiandynamics/fulcrum/tree/master/func) — Assists with functional programming
* [`fulcrum-io`](https://github.com/obsidiandynamics/fulcrum/tree/master/io) — File and socket I/O
* [`fulcrum-jgroups`](https://github.com/obsidiandynamics/fulcrum/tree/master/jgroups) — Synchronous messaging overlay for JGroups
* [`fulcrum-junit`](https://github.com/obsidiandynamics/fulcrum/tree/master/junit) — JUnit utilities
* [`fulcrum-launcher`](https://github.com/obsidiandynamics/fulcrum/tree/master/launcher) — Launch applications from Gradle
* [`fulcrum-nanoclock`](https://github.com/obsidiandynamics/fulcrum/tree/master/nanoclock) — Wall clock with nanosecond resolution
* [`fulcrum-node-queue`](https://github.com/obsidiandynamics/fulcrum/tree/master/node-queue) — Lock-free multi-producer/multi-consumer queue
* [`fulcrum-parallel`](https://github.com/obsidiandynamics/fulcrum/tree/master/parallel) — Runs jobs in parallel
* [`fulcrum-props`](https://github.com/obsidiandynamics/fulcrum/tree/master/props) — Utilities for working with `Properties`
* [`fulcrum-resolver`](https://github.com/obsidiandynamics/fulcrum/tree/master/resolver) — A lightweight Contextual Service Locator pattern implementation
* [`fulcrum-retry`](https://github.com/obsidiandynamics/fulcrum/tree/master/retry) — Retries actions that may throw a `RuntimeException`
* [`fulcrum-scheduler`](https://github.com/obsidiandynamics/fulcrum/tree/master/scheduler) — A fast task scheduler
* [`fulcrum-select`](https://github.com/obsidiandynamics/fulcrum/tree/master/select) — Functional object matching
* [`fulcrum-shell`](https://github.com/obsidiandynamics/fulcrum/tree/master/shell) — Assists in the execution of shells and processes
* [`fulcrum-testmark`](https://github.com/obsidiandynamics/fulcrum/tree/master/testmark) — Toggleable benchmark for use in unit tests
* [`fulcrum-version`](https://github.com/obsidiandynamics/fulcrum/tree/master/version) — Loads the application version and build number
* [`fulcrum-worker`](https://github.com/obsidiandynamics/fulcrum/tree/master/worker) — Worker thread with lifecycle management
* [`fulcrum-zlg`](https://github.com/obsidiandynamics/fulcrum/tree/master/zlg) — Near-zero overhead logging for performance-sensitive applications

# Getting started
Select just the module(s) that you need from the list above. Each module has a short README describing its use.

The builds are hosted on JCenter. (Maven users may need to add the JCenter repository to their POM.) Simply add the following snippet to your build file. Replace the version placeholder `x.y.z` in the snippet with the version shown on the Download badge at the top of this README, and `fulcrum-module` with the name of the actual module you need.

For Maven:

```xml
<dependency>
  <groupId>com.obsidiandynamics.fulcrum</groupId>
  <artifactId>fulcrum-module</artifactId>
  <version>x.y.z</version>
  <type>pom</type>
</dependency>
```

For Gradle:

```groovy
compile 'com.obsidiandynamics.fulcrum:fulcrum-module:x.y.z'
```
