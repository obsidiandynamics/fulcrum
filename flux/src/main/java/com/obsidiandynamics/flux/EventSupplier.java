package com.obsidiandynamics.flux;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@FunctionalInterface
public interface EventSupplier<E> {
  void get(EmissionContext<? super E> context) throws FluxException, InterruptedException;
  
  static <E> EventSupplier<E> supplier(Supplier<? extends E> supplier) {
    return context -> context.emit(supplier.get());
  }
  
  static <E> EventSupplier<E> singleton(E event) {
    return new EventSupplier<E>() {
      private boolean supplied;
      
      @Override
      public void get(EmissionContext<? super E> context) {
        if (! supplied) {
          supplied = true;
          context.emit(event);
        } else {
          context.terminate();
        }
      }
    };
  }
  
  @SafeVarargs
  static <E> EventSupplier<E> array(E... array) {
    return new EventSupplier<E>() {
      private int index = 0;
      
      @Override
      public void get(EmissionContext<? super E> context) {
        if (index < array.length) {
          context.emit(array[index++]);
        } else {
          context.terminate();
        }
      }
    };
  }
  
  static <E> EventSupplier<E> iterable(Iterable<? extends E> iterable) {
    return iterator(iterable.iterator());
  }

  static <E> EventSupplier<E> stream(Stream<? extends E> stream) {
    return iterator(stream.iterator());
  }
  
  static <E> EventSupplier<E> iterator(Iterator<? extends E> iterator) {
    return context -> {
      if (iterator.hasNext()) {
        context.emit(iterator.next());
      } else {
        context.terminate();
      }
    };
  }
}
