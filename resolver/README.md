<img src="https://raw.githubusercontent.com/wiki/obsidiandynamics/fulcrum/images/fulcrum-logo.png" width="90px" alt="logo"/> `fulcrum-resolver`
===
Lightweight implementation of the Contextual Service Locator pattern.

# Background
A Contextual Service Locator (CSL) is a dependency management pattern enabling distinct parts of an application that are not otherwise directly aware of one another to share services (behaviour, state, I/O, resources, etc.). In other words, parts of an application that have no line-of-sight communication, and are not mutually reachable through any form of shared data structure over which the application developer
has any control. The dependency provider does not instantiate the dependency consumer directly or via any DI container; neither party is
able to obtain a reference to the other through any of the conventional means. However, the parties share a JVM and a class loader.

Unlike a traditional Service Locator, a CSL isn't a 'static' singleton, and so doesn't negatively impact qualities such as testability or maintainability, or struggle in a multi-threaded environment. A CSL is an alternate means of directed communication within a JVM that plays well with such notions as components, services, IoC, concurrency and unit tests.

# How it works
A _traditional_ Service Locator revolves around a singleton _registry_ — essentially a globally-scoped hash map. Anything can read from and write to a registry.

A CSL adds two dimensions to a Service Locator — _scopes_ and _contexts_.

A **context** represents a token session that is notionally shared between a dependency supplier and a dependency consumer. The simplest (and default) form of context used by Resolver is a thread (specifically, the thread's identity).

A **scope** comprises rules for binding a context to a dedicated registry. Needless to say, registries in CSL aren't singletons. There are as many distinct registries in any given scope as there are contexts under which that scope is accessed.

When performing a dependency lookup or assignment, the caller selects an appropriate scope and passes its context. Provided that the counterparty passes the same context for that scope, the parties will share a common registry and will have access to each other's services.

# Getting started
The following example illustrates the sharing of a hypothetical catalogue of books by way of thread scope.

The catalogue is a simple mapping of titles to ISBNs:

```java
public final class Catalogue extends HashMap<String, String> {
  private static final long serialVersionUID = 1L;
}
```

Somewhere in the beginning of the application, before the catalogue is going to be consumed, we must initialise it using the `assign()` method.

```java
final Catalogue catalogue = new Catalogue();
catalogue.put("Hitchhiker's guide to the galaxy", "9782207503409");
catalogue.put("Java performance and scalability", "9781482348019");
catalogue.put("Java performance: the definitive guide", "9781449358457");

// register the catalogue in thread scope
Resolver.scope(Scope.THREAD).assign(Catalogue.class, Singleton.of(catalogue));
```

Later, when we are ready to consume the catalogue, it's supplier can be retrieved with `lookup()`.

```java
final Supplier<Catalogue> catalogueSupp = Resolver.scope(Scope.THREAD).lookup(Catalogue.class);

// print all books about Java
catalogueSupp.get().entrySet().stream().filter(e -> e.getKey().contains("Java")).forEach(System.out::println);
```

Finally, when we're sure that the catalogue will no longer be needed, we can purge it with `reset()`.

```java
Resolver.scope(Scope.THREAD).reset(Catalogue.class);
```

Note that since `Scope.THREAD` is the default scope in Resolver, the call to `Resolver.scope(Scope)` may be omitted altogether. In other words, the assignment, lookup and reset actions in the above examples can be simplified to the following.

```java
Resolver.assign(Catalogue.class, Singleton.of(catalogue));
Resolver.lookup(Catalogue.class);
Resolver.reset(Catalogue.class);
```

# Scopes
Resolver presently supports two scopes — _thread_ and _thread group_.

## Thread scope
Scopes the lookup context to a thread of execution, exhibiting behaviour analogous to a `ThreadLocal` (upon which this implementation is based).

Context passing for this scope is implicit; merely calling the `Resolver.scope(Scope)` method is sufficient for the resolver to extract the thread's identity from the caller's execution thread behind the scenes. 

This is the default scope, and one that we have used in our examples.

## Thread group scope
Thread scope is appropriate when sharing occurs within the context of a single execution thread, and is independent of all other threads in the JVM. Sometimes we need to create child threads or run tasks from a thread pool.

Java doesn't have a _persistent_ notion of a thread hierarchy. (Parent-child relationships are only considered during thread initialisation; the child thread inherits certain attributes of its parent, but following initialisation the reference to the parent is discarded.) Instead, Java offers a `ThreadGroup` — a hierarchical container for related threads (and related thread groups).

The thread group scope binds the lookup context to a thread group, allowing related (by group) threads to share the same context.

Like the thread scope, context passing for this scope is implicit; calling the `Resolver.scope(Scope)` method is sufficient for the resolver to extract the thread group's identity from the caller's execution thread behind the scenes. 

There are a few notable caveats when using thread group scope.

### Excessive sharing
If a thread group is not passed to the constructor of `Thread`, the new thread assumes the group of its creator. While this is useful for spawning child threads, it also applies to the spawning thread itself and, by extension, its parent. Unless a `ThreadGroup` is created and assigned explicitly, using `Scope.THREAD_GROUP` may have an unintended effect of _excessive sharing_. In the extreme scenario where every thread in the application has been spawned using the default thread group, `Scope.THREAD_GROUP` would be reduced to an application-wide singleton.

To suppress the sharing of registry with the parent hierarchy, use the _parent-surrogate-children pattern_, as illustrated below.

```java
final ThreadGroup surrogateGroup = new ThreadGroup("surrogate");
surrogateGroup.setDaemon(true);
final Thread surrogateThread = new Thread(surrogateGroup, () -> {
  // assignment in the surrogate thread
  Resolver.scope(Scope.THREAD_GROUP).assign(String.class, Singleton.of("surrogate and children only"));

  // lookup in the surrogate (should work)
  System.out.println("surrogate: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
  
  final ExecutorService exec = Executors.newFixedThreadPool(2);
  final int numChildren = 2;
  IntStream.range(0, numChildren).forEach(child -> exec.submit(() -> {
    // lookup in the child (should work)
    System.out.println("child: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
  }));

  exec.shutdown();
});
surrogateThread.start();
surrogateThread.join();

// lookup in the parent (should fail)
System.out.println("parent: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
```

This pattern creates an intermediate _surrogate_ thread that is responsible for spawning child threads. The surrogate is assigned an explicit thread group that is different from the parent thread. The child threads simply inherit the surrogate's group. Any dependencies that the child threads need would be injected by the surrogate, using any direct references sourced from the enclosing scope (which would typically come from the parent).

Running this code produces the following output, clearly showing that the parent has been cut off from the children.

```
surrogate: surrogate and children only
child: surrogate and children only
child: surrogate and children only
parent: null
```

### Garbage collection and daemon thread groups
The lifespan of a thread group is at least as long as that of its threads. However, if the thread group is not a daemon group, it will persist for the lifetime of the JVM. Because thread group context binds a registry directly to the thread group's identity, a non-daemon thread group will render its registry ineligible for garbage collection. When creating a new `ThreadGroup` instance, make sure you set its daemon flag explicitly, or otherwise inherit from a daemon parent thread group. Beware, the default (main) thread group is _not_ a daemon.

### Use with shared executors
Java offers built-in common thread pools (such as `ForkJoinPool.commonPool()`) for _ad hoc_  parallel processing tasks. As a general rule, common thread pools are not suitable for use with Resolver's current scope offerings. Resolver and CSL aside, the large-scale use of built-in common pools is a generally discouraged programming practice.

### Use with custom executors
By default, both fork-join and conventional executors spawn threads with the parent thread group. In this manner, the parent-surrogate-children pattern will work as expected. The example below demonstrates this.

```java
final ThreadGroup surrogateGroup = new ThreadGroup("surrogate");
surrogateGroup.setDaemon(true);
final Thread surrogateThread = new Thread(surrogateGroup, () -> {
  // assignment in the surrogate thread
  Resolver.scope(Scope.THREAD_GROUP).assign(String.class, Singleton.of("surrogate and children only"));

  // lookup in the surrogate (should work)
  System.out.println("surrogate: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
  
  final ExecutorService exec = Executors.newFixedThreadPool(2);
  final int numChildren = 2;
  IntStream.range(0, numChildren).forEach(child -> exec.submit(() -> {
    // lookup in the child (should work)
    System.out.println("child: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
  }));

  exec.shutdown();
});
surrogateThread.start();
surrogateThread.join();

// lookup in the parent (should fail)
System.out.println("parent: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
```

If a particular executor doesn't preserve the parent's thread group, then this can typically be overridden by supplying an appropriate `ThreadFactory`.

# Singleton suppliers
Resolver is largely driven by the _abstract factory pattern_; rather than providing concrete instances to satisfy a dependency, the user provides a `Supplier` factory for creating instances as required — as many as needed. This approach is significantly more flexible; however, often we just want to provide a singleton dependency instance that is to be shared by all consumers within a matching scope and context. 

`Singleton` is a convenience class for generating a `Supplier` of singleton values, with an option of either an _eager_ or a _lazy_ supplier. 

Calling `Singleton.of(T)` (where `T` is the supplied value type) will produce an eager singleton. The same value will be returned for all future invocations of `get()` on the resulting `Supplier` instance.

Alternatively, calling `Singleton.of(Supplier<T>)` will produce a lazy singleton. The value will be lazily instantiated upon first use (the first call to `get()`), and will subsequently be reused for all future calls to `get()`. Lazy singletons are thread-safe, and are guaranteed to be instantiated at most once.


# Where CSL may be used
Note: this is not a discussion on the merits of container-based/IoC DI _versus_ CSL _versus_ plain constructor/setter use. (Although we'll agree that traditional Service Locators are a general anti-pattern.) The following are some prospective scenarios where CSL is either objectively more convenient or the only practically viable approach.

* **Framework-driven code with little/no developer control.** Where an application's classes are instantiated by an external framework (typically by way of reflection or through abstract factories), the key application components may not be directly aware of each other (have no traversable references in either direction). A good example (one we recently faced) is attempting to unit test benchmark code that runs in a JMH harness. JMH provides means for running benchmarks in the same JVM as the initiator without forking a new JVM (used strictly for testing); however, it doesn't permit any form of reference passing from the initiator code to the benchmark instance. (Understandably so, as microbenchmarks are meant to be self-contained.) We used Resolver with thread group scope to feed a dependency to the benchmark code with no changes to the harness.

* **Retrofitting applications with deeply nested dependency consumers.** Applications that haven't been built with DI containers in mind may require significant refactoring to add support for DI. Sometimes the effort simply isn't justified by the benefit, particularly when dependency consumers are deeply nested. (Irrespective of whether or not a DI container is the appropriate solution.)

* **Working with concurrent code.** IoC-based DI is rarely used in concurrent and/or asynchronous applications, where the dependency consumers operate in a different threading context to dependency providers. That's because in a concurrent/asynchronous environments, dependent objects may be instantiated on demand, long after the DI container has completed its work (materialising an object graph). So while it is still technically possible to use DI, injection is typically done at a factory (rather than instance) level. (This way dependencies can still be fed to emergent consumers through factories even after the DI context has been disposed of.) This does feel unnatural, since the dependencies aren't actually supplied by the container; the latter is exploited as a second order _supplier of suppliers_. Furthermore, the lifecycle of the DI container does not align with the lifecycle of the provided dependencies. For these scenarios, using a CSL provides a natural alternative, being inherently factory-driven, with a lifecycle that transcends the construction of a single object graph.

* **You wish to escape the Church of DI+IoC...** Inverted DI (using containers, to be precise) has been one of the more overused patterns in contemporary software development. While not without its merits, DI containers unequivocally add additional complexity and obscurity (often by way of reflection) to the application, and must be weighted carefully and used judiciously. Equivalently, the same must be said about CSL. Perhaps a more rational explanation for the apparent overuse is that most developers confuse the _Dependency Inversion Principle_ with _Inversion of Control_ and _Dependency Injection_ — three distinct terms, with similar sounding names.
