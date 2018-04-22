package com.obsidiandynamics.jgroups;

import java.io.*;
import java.util.*;

public abstract class SyncPacket implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private final Serializable id;
  
  protected SyncPacket(Serializable id) {
    this.id = id;
  }
  
  public final Serializable getId() {
    return id;
  }
  
  public final String baseToString() {
    return "id=" + id;
  }
  
  @Override
  public final int hashCode() {
    return Objects.hashCode(id);
  }
  
  @Override
  public final boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof SyncPacket) {
      return Objects.equals(id, ((SyncPacket) obj).id);
    } else {
      return false;
    }
  }
}
