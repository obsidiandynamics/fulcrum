package com.obsidiandynamics.random;

import static org.junit.Assert.*;

import java.security.*;
import java.util.*;
import java.util.function.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class RandomnessTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Randomness.class);
  }

  @Test
  public void testGetSecureRandom() {
    final SecureRandom rng = Randomness.getSecureRandom();
    assertNotNull(rng);
    assertSame(rng, Randomness.getSecureRandom());
  }

  @Test
  public void testNextInt() {
    Randomness.nextInt();
  }

  @Test
  public void testNextLong() {
    Randomness.nextLong();
  }

  @Test
  public void testNextBytes() {
    final int maxAttempts = 100;
    testRandomness(maxAttempts, () -> Randomness.nextBytes(1024));
  }

  /**
   *  Tests whether the given byte array is likely 'random' based on the distribution
   *  of unique bytes.
   *  
   *  @param maxAttempts The maximum number of failed attempts before concluding that the array
   *                     is unlikely to have come from a random source.
   *  @param randomness A source of randomness.
   */
  private static void testRandomness(int maxAttempts, Supplier<byte[]> randomness) {
    for (int i = 0; i < maxAttempts; i++) {
      final byte[] random = randomness.get();
      if (isWellDistributed(random)) {
        return;
      }
    }
    throw new UnlikelyRandomSourceError("Doesn't appear to be a random source");
  }

  private static boolean isWellDistributed(byte[] bytes) {
    final Set<Byte> unique = new HashSet<>(bytes.length);
    for (int i = 0; i < bytes.length; i++) {
      unique.add(bytes[i]);
    }
    final int expectedUniqueBytes = Math.min((int) Math.round(Math.sqrt(bytes.length)), 256);
    return unique.size() >= expectedUniqueBytes;
  }

  private static class UnlikelyRandomSourceError extends AssertionError {
    private static final long serialVersionUID = 1L;

    UnlikelyRandomSourceError(String m) { super(m); }
  }

  /**
   *  Self-test verifying that {@link #testRandomness()} works as expected.
   */
  @Test(expected=UnlikelyRandomSourceError.class)
  public void testRandomnessFailure() {
    testRandomness(1, () -> new byte[10]);
  }
}
