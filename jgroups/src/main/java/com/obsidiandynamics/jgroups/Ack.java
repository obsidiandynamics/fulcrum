package com.obsidiandynamics.jgroups;

import java.io.*;

public final class Ack extends SyncPacket {
  private static final long serialVersionUID = 1L;
  
  private Ack(Serializable id) {
    super(id);
  }

  public static Ack of(SyncPacket message) {
    return new Ack(message.getId());
  }

  public static Ack forId(Serializable id) {
    return new Ack(id);
  }
  
  @Override
  public String toString() {
    return Ack.class.getSimpleName() + " [" + baseToString() + "]";
  }
}
