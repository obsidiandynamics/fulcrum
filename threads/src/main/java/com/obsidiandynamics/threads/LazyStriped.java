package com.obsidiandynamics.threads;

import static com.obsidiandynamics.func.Functions.*;

import java.util.concurrent.atomic.*;
import java.util.function.*;

/**
 *  A {@link Striped} implementation that lazily initialises the striped values.
 *
 *  @param <S> The type of striped value.
 */
public final class LazyStriped<S> implements Striped<S> {
  private final Supplier<? extends S> valueSupplier;
  
  private final AtomicReferenceArray<S> refArray;
  
  public LazyStriped(int stripes, Supplier<? extends S> valueSupplier) {
    this.valueSupplier = mustExist(valueSupplier, "Value supplier cannot be null");
    mustBeGreaterOrEqual(stripes, 0, illegalArgument("Number of stripes cannot be negative"));
    refArray = new AtomicReferenceArray<>(stripes);
  }
  
  @Override
  public S get(int keyHash) {
    final int stripe = Striped.resolveStripe(keyHash, refArray.length());
    final S existing = refArray.get(stripe);
    if (existing != null) {
      return existing;
    } else {
      final S newValue = valueSupplier.get();
      
      // note: when upgrading to JDK 11, replace compareAndSet() with compareAndExchange()
      if (refArray.compareAndSet(stripe, null, newValue)) {
        return newValue;
      } else {
        return refArray.get(stripe);
      }
    }
  }
}
