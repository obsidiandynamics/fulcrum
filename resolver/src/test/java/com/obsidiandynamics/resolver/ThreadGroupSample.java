package com.obsidiandynamics.resolver;

import java.util.stream.*;

public final class ThreadGroupSample {
  public static void main(String[] args) throws InterruptedException {
    parentNannyChildren();
  }
  
  private static void parentNannyChildren() throws InterruptedException {
    final ThreadGroup nannyGroup = new ThreadGroup("nanny");
    nannyGroup.setDaemon(true);
    final Thread nannyThread = new Thread(nannyGroup, () -> {
      // assignment in the nanny thread
      Resolver.scope(Scope.THREAD_GROUP).assign(String.class, Singleton.of("nanny and children only"));
      
      // lookup in the nanny (should work)
      System.out.println("nanny: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
      
      final int numChildren = 2;
      IntStream.range(0, numChildren).forEach(child -> new Thread(() -> {
        // lookup in the child (should work)
        System.out.println("child: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
      }).start());
    });
    nannyThread.start();
    nannyThread.join();
    
    // lookup in the parent (should fail)
    System.out.println("parent: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
  }
}
