<img src="https://raw.githubusercontent.com/wiki/obsidiandynamics/fulcrum/images/fulcrum-logo.png" width="90px" alt="logo"/> Fulcrum
===
Fulcrum is home to tiny **micro libraries** that do very specific things, and can be imported into a project individually, without dragging in tons of unneeded dependencies.

[![Maven release](https://img.shields.io/maven-metadata/v.svg?color=blue&label=maven-central&metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fcom%2Fobsidiandynamics%2Ffulcrum%2Ffulcrum-assert%2Fmaven-metadata.xml)](https://mvnrepository.com/artifact/com.obsidiandynamics.fulcrum)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/obsidiandynamics/fulcrum.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/obsidiandynamics/fulcrum/alerts/)
[![Gradle build](https://github.com/obsidiandynamics/fulcrum/actions/workflows/master.yml/badge.svg)](https://github.com/obsidiandynamics/fulcrum/actions/workflows/master.yml)
[![codecov](https://codecov.io/gh/obsidiandynamics/fulcrum/branch/master/graph/badge.svg)](https://codecov.io/gh/obsidiandynamics/fulcrum)

# Modules
* [`fulcrum-assert`](https://github.com/obsidiandynamics/fulcrum/tree/master/assert) — Common assertions
* [`fulcrum-await`](https://github.com/obsidiandynamics/fulcrum/tree/master/await) — Utility for awaiting asynchronous actions
* [`fulcrum-combinations`](https://github.com/obsidiandynamics/fulcrum/tree/master/combinations) — Generates combinations of elements in a 2D array
* [`fulcrum-concat`](https://github.com/obsidiandynamics/fulcrum/tree/master/concat) — Fluid API for selective concatenation of strings
* [`fulcrum-constraints`](https://github.com/obsidiandynamics/fulcrum/tree/master/constraints) — Helper for working with `javax.validation`
* [`fulcrum-docker-compose`](https://github.com/obsidiandynamics/fulcrum/tree/master/docker-compose) — Wrapper around the `docker-compose` CLI
* [`fulcrum-dyno`](https://github.com/obsidiandynamics/fulcrum/tree/master/dyno) — Micro-benchmarking harness
* [`fulcrum-flow`](https://github.com/obsidiandynamics/fulcrum/tree/master/flow) — Strictly ordered joining of parallel tasks
* [`fulcrum-flux`](https://github.com/obsidiandynamics/fulcrum/tree/master/flux) — Reactive, SEDA-style pipeline
* [`fulcrum-format`](https://github.com/obsidiandynamics/fulcrum/tree/master/format) — Formatting functions and utilities
* [`fulcrum-fslock`](https://github.com/obsidiandynamics/fulcrum/tree/master/fslock) — Reentrant, interprocess exclusive locking protocol
* [`fulcrum-fslock-offheap`](https://github.com/obsidiandynamics/fulcrum/tree/master/fslock-offheap) — Off-heap support for Flux
* [`fulcrum-func`](https://github.com/obsidiandynamics/fulcrum/tree/master/func) — Assists with functional programming
* [`fulcrum-httpclient`](https://github.com/obsidiandynamics/fulcrum/tree/master/httpclient) — Utilities for working with Apache HttpClient
* [`fulcrum-io`](https://github.com/obsidiandynamics/fulcrum/tree/master/io) — File and socket I/O
* [`fulcrum-jgroups`](https://github.com/obsidiandynamics/fulcrum/tree/master/jgroups) — Synchronous messaging overlay for JGroups
* [`fulcrum-json`](https://github.com/obsidiandynamics/fulcrum/tree/master/json) — JSON parsing and formatting using Jackson APIs
* [`fulcrum-junit`](https://github.com/obsidiandynamics/fulcrum/tree/master/junit) — JUnit 4.x utilities
* [`fulcrum-launcher`](https://github.com/obsidiandynamics/fulcrum/tree/master/launcher) — Launch applications from Gradle
* [`fulcrum-mockito`](https://github.com/obsidiandynamics/fulcrum/tree/master/mockito) — Mockito 2.x utilities
* [`fulcrum-nanoclock`](https://github.com/obsidiandynamics/fulcrum/tree/master/nanoclock) — Wall clock with nanosecond resolution
* [`fulcrum-nodequeue`](https://github.com/obsidiandynamics/fulcrum/tree/master/nodequeue) — Lock-free multi-producer/multi-consumer queue
* [`fulcrum-props`](https://github.com/obsidiandynamics/fulcrum/tree/master/props) — Utilities for working with `Properties`
* [`fulcrum-random`](https://github.com/obsidiandynamics/fulcrum/tree/master/random) — Secure random number generation
* [`fulcrum-resolver`](https://github.com/obsidiandynamics/fulcrum/tree/master/resolver) — A lightweight Contextual Service Locator pattern implementation
* [`fulcrum-retry`](https://github.com/obsidiandynamics/fulcrum/tree/master/retry) — Retries actions that may throw a `RuntimeException`
* [`fulcrum-scheduler`](https://github.com/obsidiandynamics/fulcrum/tree/master/scheduler) — A fast task scheduler
* [`fulcrum-select`](https://github.com/obsidiandynamics/fulcrum/tree/master/select) — Functional object matching
* [`fulcrum-shell`](https://github.com/obsidiandynamics/fulcrum/tree/master/shell) — Assists in the execution of shells and processes
* [`fulcrum-testmark`](https://github.com/obsidiandynamics/fulcrum/tree/master/testmark) — Toggleable benchmark for use in unit tests
* [`fulcrum-threads`](https://github.com/obsidiandynamics/fulcrum/tree/master/threads) — Utilities for working with threads
* [`fulcrum-verifier`](https://github.com/obsidiandynamics/fulcrum/tree/master/verifier) — Verifiers of conventional class/object behaviour
* [`fulcrum-version`](https://github.com/obsidiandynamics/fulcrum/tree/master/version) — Loads the application version and build number
* [`fulcrum-worker`](https://github.com/obsidiandynamics/fulcrum/tree/master/worker) — Worker thread with lifecycle management

# Getting started
Select just the module(s) that you need from the list above. Some modules have a short README describing their use.

Simply add the following snippet to your build file. Replace the version placeholder `x.y.z` in the snippet with the version shown on the Download badge at the top of this README, and `fulcrum-module` with the name of the actual module you need.

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
api 'com.obsidiandynamics.fulcrum:fulcrum-module:x.y.z'
```
