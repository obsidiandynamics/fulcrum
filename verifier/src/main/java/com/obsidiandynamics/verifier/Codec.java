package com.obsidiandynamics.verifier;

public interface Codec {
  byte[] toBytes(Object obj) throws Exception;
  
  <T> T toObject(byte[] bytes, Class<T> type) throws Exception;
}