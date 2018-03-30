package com.obsidiandynamics.select;

import java.util.function.*;

import com.obsidiandynamics.func.*;

public final class ValueThen<S extends SelectRoot<R>, V, R> {
  private final S select;
  private final V value;
  private final boolean fire;

  ValueThen(S select, V value, boolean fire) {
    this.select = select;
    this.value = value;
    this.fire = fire;
  }
  
  public S then(Consumer<? super V> action) {
    return thenReturn(value -> {
      action.accept(value);
      return null;
    });
  }
  
  public S thenReturn(Function<? super V, ? extends R> action) {
    if (fire) {
      select.setReturn(action.apply(value));
    }
    return select;
  }
  
  public S thenThrow(Supplier<? extends RuntimeException> exceptionSupplier) {
    return then(Select.throwFromConsumer(exceptionSupplier));
  }
  
  public <W> ValueThen<S, W, R> transform(Function<? super V, ? extends W> transform) {
    final W newValue = fire ? transform.apply(value) : null;
    return new ValueThen<>(select, newValue, fire);
  }
  
  public final class Checked {
    Checked() {}
    
    public <X extends Exception> S then(CheckedConsumer<? super V, X> action) throws X {
      return thenReturn(value -> {
        action.accept(value);
        return null;
      });
    }
    
    public <X extends Exception> S thenReturn(CheckedFunction<? super V, ? extends R, X> action) throws X {
      if (fire) {
        select.setReturn(action.apply(value));
      }
      return select;
    }
    
    public <X extends Exception> S thenThrow(Supplier<X> exceptionSupplier) throws X {
      return then(Select.throwCheckedFromConsumer(exceptionSupplier));
    }
    
    public <W, X extends Exception> ValueThen<S, W, R>.Checked transform(CheckedFunction<? super V, ? extends W, X> transform) throws X {
      final W newValue = fire ? transform.apply(value) : null;
      return new ValueThen<>(select, newValue, fire).checked();
    }
  }
  
  <U> Checked checked() {
    return new Checked();
  }
}
