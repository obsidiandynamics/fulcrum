package com.obsidiandynamics.resolver;

import java.util.*;
import java.util.function.*;

/**
 *  Illustrates the sharing of a hypothetical catalogue of books (mapping of titles to ISBNs)
 *  by way of thread scope.
 */
public final class ResolverSample {
  private static class Catalogue extends HashMap<String, String> {
    private static final long serialVersionUID = 1L;
  }
  
  private static void somewhereInTheBeginning() {
    final Catalogue catalogue = new Catalogue();
    catalogue.put("Hitchhiker's guide to the galaxy", "9782207503409");
    catalogue.put("Java performance and scalability", "9781482348019");
    catalogue.put("Java performance: the definitive guide", "9781449358457");
    
    // register the catalogue in thread scope
    Resolver.scope(Scope.THREAD).assign(Catalogue.class, Singleton.of(catalogue));
  }
  
  private static void laterInTheApplication() {
    final Supplier<Catalogue> catalogueSupp = Resolver.scope(Scope.THREAD).lookup(Catalogue.class);
    
    // print all books about Java
    catalogueSupp.get().entrySet().stream().filter(e -> e.getKey().contains("Java")).forEach(System.out::println);
  }
  
  private static void somewhereAtTheEnd() {
    Resolver.scope(Scope.THREAD).reset(Catalogue.class);
  }
  
  public static void main(String[] args) {
    somewhereInTheBeginning();
    laterInTheApplication();
    somewhereAtTheEnd();
  }
}
