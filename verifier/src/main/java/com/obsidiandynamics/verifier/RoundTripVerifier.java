package com.obsidiandynamics.verifier;

import static com.obsidiandynamics.func.Functions.*;

import java.util.*;

import com.obsidiandynamics.func.*;

public final class RoundTripVerifier<T> {
  private final T object;
  
  private Class<? super T> type;
  
  private Codec codec;
  
  private RoundTripVerifier(T object) {
    this.object = object;
    type = Classes.cast(object.getClass());
  }
  
  public RoundTripVerifier<T> withType(Class<? super T> type) {
    this.type = type;
    return this;
  }
  
  public RoundTripVerifier<T> withCodec(Codec codec) {
    this.codec = codec;
    return this;
  }
  
  public static <T> RoundTripVerifier<T> forObject(T object) {
    return new RoundTripVerifier<>(object);
  }
  
  static final class CodecIOError extends AssertionError {
    private static final long serialVersionUID = 1L;
    
    CodecIOError(Throwable cause) { super(cause); }
  }
  
  public void verify() {
    final Codec _codec = mustExist(this.codec, "Codec not set");
    
    final Object decoded = Exceptions.wrap(() -> {
      final byte[] bytes = _codec.toBytes(object);
      return _codec.toObject(bytes, type);
    }, CodecIOError::new);
    
    if (! Objects.equals(object, decoded)) throw new AssertionError("Expected '" + object + "', got: '" + decoded + "'");
  }
}
