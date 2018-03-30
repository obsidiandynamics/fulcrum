package com.obsidiandynamics.select;

import java.util.function.*;

import com.obsidiandynamics.func.*;

public final class NullThen<S extends SelectRoot<R>, R> {
  private final S select;
  private final boolean fire;

  NullThen(S select, boolean fire) {
    this.select = select;
    this.fire = fire;
  }
  
  public S then(Runnable action) {
    return thenReturn(() -> {
      action.run();
      return null;
    });
  }
  
  public S thenReturn(Supplier<? extends R> action) {
    if (fire) {
      select.setReturn(action.get());
    }
    return select;
  }
  
  public S thenThrow(Supplier<? extends RuntimeException> exceptionSupplier) {
    return then(Select.throwFromRunnable(exceptionSupplier));
  }
  
  public final class Checked {
    Checked() {}
    
    public <X extends Exception> S then(CheckedRunnable<X> action) throws X {
      return thenReturn(() -> {
        action.run();
        return null;
      });
    }
    
    public <X extends Exception> S thenReturn(CheckedSupplier<? extends R, X> action) throws X {
      if (fire) {
        select.setReturn(action.get());
      }
      return select;
    }
    
    public <X extends Exception> S thenThrow(Supplier<X> exceptionSupplier) throws X {
      return then(Select.throwCheckedFromRunnable(exceptionSupplier));
    }
  }
  
  Checked checked() {
    return new Checked();
  }
}
