package com.obsidiandynamics.flux;

import java.util.*;
import java.util.function.*;

public final class Sinks {
  private Sinks() {}
  
  public static <E> ConsumerSink<E> consumer(EventConsumer<? super E> eventConsumer) {
    return new ConsumerSink<E>(eventConsumer);
  }
  
  public static <E> ConsumerSink<E> consumer(Consumer<? super E> consumer) {
    return consumer(EventConsumer.consumer(consumer));
  }
  
  public static <E> ConsumerSink<E> collection(Collection<? super E> collection) {
    return consumer(EventConsumer.collection(collection));
  }
  
  public static <E> ConsumerSink<E> nop() {
    return consumer(EventConsumer.nop());
  }
}
