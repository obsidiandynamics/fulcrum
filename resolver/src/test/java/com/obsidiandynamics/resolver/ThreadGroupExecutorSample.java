package com.obsidiandynamics.resolver;

import java.util.concurrent.*;
import java.util.stream.*;

public final class ThreadGroupExecutorSample {
  public static void main(String[] args) throws InterruptedException {
    parentNannyChildrenWithExecutor();
  }
  
  private static void parentNannyChildrenWithExecutor() throws InterruptedException {
    final ThreadGroup nannyGroup = new ThreadGroup("nanny");
    nannyGroup.setDaemon(true);
    final Thread nannyThread = new Thread(nannyGroup, () -> {
      // assignment in the nanny thread
      Resolver.scope(Scope.THREAD_GROUP).assign(String.class, Singleton.of("nanny and children only"));

      // lookup in the nanny (should work)
      System.out.println("nanny: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
      
      final ExecutorService exec = Executors.newFixedThreadPool(2);
      final int numChildren = 2;
      IntStream.range(0, numChildren).forEach(child -> exec.submit(() -> {
        // lookup in the child (should work)
        System.out.println("child: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
      }));

      exec.shutdown();
    });
    nannyThread.start();
    nannyThread.join();
    
    // lookup in the parent (should fail)
    System.out.println("parent: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
  }
}
