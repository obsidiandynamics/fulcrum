package com.obsidiandynamics.flux;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public final class Emitters {
  private Emitters() {}
  
  public static <E> SupplierEmitter<E> supplier(EventSupplier<? extends E> eventSupplier) {
    return new SupplierEmitter<>(eventSupplier);
  }
  
  public static <E> SupplierEmitter<E> supplier(Supplier<? extends E> supplier) {
    return supplier(EventSupplier.supplier(supplier));
  }
  
  public static <E> SupplierEmitter<E> singleton(E event) {
    return supplier(EventSupplier.singleton(event));
  }
  
  @SafeVarargs
  public static <E> SupplierEmitter<E> array(E... array) {
    return supplier(EventSupplier.array(array));
  }
  
  public static <E> SupplierEmitter<E> iterable(Iterable<? extends E> iterable) {
    return supplier(EventSupplier.iterable(iterable));
  }

  public static <E> SupplierEmitter<E> stream(Stream<? extends E> stream) {
    return supplier(EventSupplier.stream(stream));
  }
  
  public static <E> SupplierEmitter<E> iterator(Iterator<? extends E> iterator) {
    return supplier(EventSupplier.iterator(iterator));
  }
  
  public static <E> PeriodicEmitter<E> periodic(Rate rate, Supplier<? extends E> supplier) {
    return periodic(rate, EventSupplier.supplier(supplier));
  }
  
  public static <E> PeriodicEmitter<E> periodic(Rate rate, EventSupplier<? extends E> eventSupplier) {
    return new PeriodicEmitter<>(rate, eventSupplier);
  }
}
