package com.obsidiandynamics.flux;

public interface Channel<I, O> extends Sink<I>, Emitter<O> {}
