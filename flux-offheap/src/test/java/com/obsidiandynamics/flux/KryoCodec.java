package com.obsidiandynamics.flux;

import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.io.*;
import com.obsidiandynamics.verifier.*;

final class KryoCodec implements Codec {
  private static final int DEF_BUFFER_SIZE = 128;
  
  private final Serializer<?> serializer;
  
  public KryoCodec(Serializer<?> serializer) {
    this.serializer = serializer;
  }
  
  private static Kryo createKryo() {
    final Kryo kryo = new Kryo();
    kryo.setReferences(false);
    return kryo;
  }

  @Override
  public byte[] toBytes(Object obj) throws Exception {
    final Output out = new Output(DEF_BUFFER_SIZE, -1);
    final Kryo kryo = createKryo();
    kryo.writeObject(out, obj, serializer);
    return out.toBytes();
  }

  @Override
  public <T> T toObject(byte[] bytes, Class<T> type) throws Exception {
    final Input in = new Input(bytes);
    final Kryo kryo = createKryo();
    return kryo.readObject(in, type, serializer);
  }
}