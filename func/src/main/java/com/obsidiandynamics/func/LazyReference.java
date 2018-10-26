package com.obsidiandynamics.func;

/**
 *  An efficient reference to a lazily instantiable object, using double-checked locking
 *  to guarantee at-most-once initialisation. <p>
 *  
 *  The {@link CheckedSupplier} used for referent instantiation may throw a checked exception, which
 *  will be propagated to the caller of {@link #get()}. After successful instantiation, the supplier
 *  reference will be released, allowing for it to be garbage-collected. <p>
 *  
 *  This class is thread-safe.
 *  
 *  @param <T> Reference type.
 *  @param <X> Exception type.
 */
public class LazyReference<T, X extends Throwable> {
  private CheckedSupplier<T, X> supplier;
  
  private final Object lock = new Object();
  
  private volatile T reference;

  protected LazyReference(CheckedSupplier<T, X> supplier) {
    this.supplier = supplier;
  }
  
  /**
   *  Obtains the underlying referent, instantiating it on the first request.
   *  
   *  @return The referent.
   *  @throws X If an error occurs in the supplier.
   */
  public T get() throws X {
    final T firstCheck = reference;
    if (firstCheck != null) {
      return firstCheck;
    } else {
      synchronized (lock) {
        final T secondCheck = reference;
        if (secondCheck != null) {
          return secondCheck;
        } else {
          final T created = supplier.get();
          supplier = null;
          reference = created;
          return created;
        }
      }
    }
  }
  
  /**
   *  Obtains the referent without attempting instantiation.
   *  
   *  @return The referent, or {@code null}.
   */
  public T peek() {
    return reference;
  }
  
  @Override
  public String toString() {
    return LazyReference.class + " [reference=" + reference + "]";
  }
  
  public static <T, X extends Throwable> LazyReference<T, X> from(CheckedSupplier<T, X> supplier) {
    return new LazyReference<>(supplier);
  }
}
