package com.obsidiandynamics.threads;

import static com.obsidiandynamics.func.Functions.*;

/**
 *  A deterministic reduction of a hashable key (in a standard 32-bit hash space) to an 
 *  object in a stripe, where the stripe space is typically much smaller than the hash
 *  space. <p>
 *  
 *  A {@link Striped} implementation guarantees that any given key will always map to the same
 *  value, once the mapping has been reified. <p>
 *  
 *  Implementations are required to be thread-safe.
 *
 *  @param <S> The type of striped value.
 *  
 *  @see EagerStriped
 *  @see LazyStriped
 */
public interface Striped<S> {
  /**
   *  Obtains the striped value for a given hashable key.
   *  
   *  @param key The hashable key.
   *  @return The corresponding striped value.
   */
  default S get(Object key) {
    return get(mustExist(key, "Key cannot be null").hashCode());
  }
  
  /**
   *  Obtains the striped value for the given key hash.
   *  
   *  @param keyHash The key hash.
   *  @return The corresponding striped value.
   */
  S get(int keyHash);
  
  /**
   *  Obtains stripe number that a particular hash reduces to, given the number
   *  of stripes.
   *  
   *  @param hash The hash to reduce.
   *  @param stripes The number of stripes.
   *  @return The stripe index, in the range of {@code 0} to {@code stripes - 1} (inclusive).
   */
  static int resolveStripe(int hash, int stripes) {
    // gets a better hash spread (lifted from HashMap), without adversely impacting the quality of already-good hashes
    final int spreadHash = hash ^ (hash >>> 16);
    return ((spreadHash % stripes) + stripes) % stripes;
  }
}
