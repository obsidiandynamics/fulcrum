package com.obsidiandynamics.resolver;

import java.util.stream.*;

public final class ThreadGroupSample {
  public static void main(String[] args) throws InterruptedException {
    parentSurrogateChildren();
  }
  
  private static void parentSurrogateChildren() throws InterruptedException {
    final ThreadGroup surrogateGroup = new ThreadGroup("surrogate");
    surrogateGroup.setDaemon(true);
    final Thread surrogateThread = new Thread(surrogateGroup, () -> {
      // assignment in the surrogate thread
      Resolver.scope(Scope.THREAD_GROUP).assign(String.class, Singleton.of("surrogate and children only"));
      
      // lookup in the surrogate (should work)
      System.out.println("surrogate: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
      
      final int numChildren = 2;
      IntStream.range(0, numChildren).forEach(child -> new Thread(() -> {
        // lookup in the child (should work)
        System.out.println("child: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
      }).start());
    });
    surrogateThread.start();
    surrogateThread.join();
    
    // lookup in the parent (should fail)
    System.out.println("parent: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
  }
}
