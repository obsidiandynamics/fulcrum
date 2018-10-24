package com.obsidiandynamics.random;

import java.security.*;

/**
 *  Securely generates random integral numbers and byte arrays using
 *  a singleton {@link SecureRandom} instance. <p>
 *  
 *  This class is thread-safe.
 */
public final class Randomness {
  /**
   *  The random number generator used by this class, contained in a holder class 
   *  to defer initialisation until needed.
   */
  private static class Holder {
    static final SecureRandom random = new SecureRandom();
  }
  
  private Randomness() {}
  
  /**
   *  Obtains a reference to the underlying RNG.
   *  
   *  @return The singleton {@link SecureRandom} instance.
   */
  public static SecureRandom getSecureRandom() {
    return Holder.random;
  }
  
  /**
   *  Generates a random {@code int}.
   *  
   *  @return A random {@code int}.
   */
  public static int nextInt() {
    return Holder.random.nextInt();
  }
  
  /**
   *  Generates a random {@code long}.
   *  
   *  @return A random {@code long}.
   */
  public static long nextLong() {
    return Holder.random.nextLong();
  }
  
  /**
   *  Produces an array of random bytes.
   *  
   *  @param length The length of the byte array.
   *  @return The random byte array.
   */
  public static byte[] nextBytes(int length) {
    final byte[] bytes = new byte[length];
    Holder.random.nextBytes(bytes);
    return bytes;
  }
}