package com.obsidiandynamics.flux;

import java.util.*;
import java.util.function.*;

@FunctionalInterface
public interface EventConsumer<E> {
  void accept(StageContext context, E event) throws InterruptedException, FluxException;
  
  static <E> EventConsumer<E> consumer(Consumer<? super E> consumer) {
    return (__context, event) -> consumer.accept(event);
  }
  
  static <E> EventConsumer<E> collection(Collection<? super E> collection) {
    return consumer(collection::add);
  }
  
  static <E> EventConsumer<E> nop() {
    return (__context, __event) -> {};
  }
}
