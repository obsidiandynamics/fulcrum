<img src="https://raw.githubusercontent.com/wiki/obsidiandynamics/fulcrum/images/fulcrum-logo.png" width="90px" alt="logo"/> `fulcrum-resolver`
===
Lightweight implementation of the Contextual Service Locator pattern.

# Background
A Contextual Service Locator (CSL) is a dependency management pattern enabling distinct parts of an application that are not otherwise directly aware of one another to share services (behaviour, state, I/O, etc.). In other words, parts of an application that have no line-of-sight communication, and are not mutually reachable through any form of shared data structure over which the application developer
has any control. The dependency supplier does not instantiate the dependency consumer directly or via any DI container; neither party is
able to obtain a reference to the other through any of the conventional means. However, the parties share a JVM and a class loader.

Unlike a traditional Service Locator, a CSL isn't 'static' like a singleton, and so doesn't negatively impact qualities such as testability or maintainability and struggles in a multi-threaded environment. A CSL is an alternate means of directed communication within a JVM that plays well with such notions as components, services, concurrency and unit tests.

# How it works
A traditional Service Locator revolves around a singleton _registry_ — essentially a globally-scoped hash map. Anything can read from and write to a registry.

A CSL adds two dimensions to a Service Locator — _scopes_ and _contexts_.

A **context** represents some form of a conversational session that is notionally shared between a dependency supplier and a dependency consumer. The simplest (and default) form of context used by Resolver is a thread (specifically, the thread's identity).

A **scope** comprises rules for binding a context to a dedicated registry. Needless to say, registries in CSL aren't singletons. There are as many distinct registries in any given scope as there are contexts under which that scope is accessed.

When performing a dependency lookup or assignment, the caller selects an appropriate scope and passes its context. Providing that the counterparty passes the same context for that scope, the parties will share a common registry and will have access to each other's services.

# Getting started
The following example illustrates the sharing of a hypothetical catalogue of books by way of thread scope.

The catalogue is a simple mapping of titles to ISBNs:

```java
private static class Catalogue extends HashMap<String, String> {
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

Finally, when we're sure that the catalogue will never be used, we can purge it with `reset()`.

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



# Singleton suppliers



# Where this can be used
* Frameworks with no control
* Retrofitting applications with deeply nested dependency consumers that haven't been built with DI containers
* You wish to escape the Church of Dependency Injection...
