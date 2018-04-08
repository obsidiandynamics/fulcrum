package com.obsidiandynamics.resolver;

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
      
      // lookup in the parent
      System.out.println("parent: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
      
      new Thread(() -> {
        // lookup in the child
        System.out.println("child 1: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
      }).start();
      
      new Thread(() -> {
        // lookup in the child
        System.out.println("child 2: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
      }).start();
    });
    nannyThread.start();
    nannyThread.join();
    
    // lookup in the parent
    System.out.println("parent: " + Resolver.scope(Scope.THREAD_GROUP).lookup(String.class).get());
  }
}
