package com.obsidiandynamics.flux;

import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.io.*;

public final class KryoTimestampedSerializer extends Serializer<Timestamped<?>> {
  @Override
  public void write(Kryo kryo, Output output, Timestamped<?> timestamped) {
    output.writeLong(timestamped.getTimestamp());
    kryo.writeClassAndObject(output, timestamped.getValue());
  }

  @Override
  public Timestamped<?> read(Kryo kryo, Input input, Class<? extends Timestamped<?>> type) {
    final long timestamp = input.readLong();
    final Object value = kryo.readClassAndObject(input);
    return new Timestamped<>(timestamp, value);
  }
}
