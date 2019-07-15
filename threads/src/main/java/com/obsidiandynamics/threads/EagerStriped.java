package com.obsidiandynamics.threads;

import static com.obsidiandynamics.func.Functions.*;

import java.util.function.*;

import com.obsidiandynamics.func.*;

/**
 *  A {@link Striped} implementation that eagerly initialises the striped values.
 *
 *  @param <S> The type of striped value.
 */
public final class EagerStriped<S> implements Striped<S> {
  private final Object[] values;
  
  public EagerStriped(int stripes, Supplier<? extends S> valueSupplier) {
    mustExist(valueSupplier, "Value supplier cannot be null");
    mustBeGreaterOrEqual(stripes, 0, illegalArgument("Number of stripes cannot be negative"));
    values = new Object[stripes];
    for (int stripe = 0; stripe < stripes; stripe++) {
      values[stripe] = valueSupplier.get();
    }
  }
  
  @Override
  public S get(int keyHash) {
    final int stripe = Striped.resolveStripe(keyHash, values.length);
    return Classes.cast(values[stripe]);
  }
}
