package com.obsidiandynamics.format;

import java.nio.*;

/**
 *  Provides conversion and printing utilities for binary data (byte arrays).
 */
public final class Binary {
  private Binary() {}
  
  /**
   *  Verifies whether the given {@code int} lies in the allowable unsigned byte range 
   *  (0x00—0xFF).
   *  
   *  @param intToTest The number to test.
   *  @return True if the number lies in the unsigned byte range.
   */
  public static boolean isInByteRange(int intToTest) {
    return intToTest >= 0x00 && intToTest <= 0xFF;
  }
  
  public static final class NotAnUnsignedByteException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;
    
    NotAnUnsignedByteException(String m) { super(m); }
  }
  
  /**
   *  Ensures that the given {@code int} lies in the allowable unsigned byte range 
   *  (0x00—0xFF), returning the byte value if this is the case, and throwing a 
   *  {@link NotAnUnsignedByteException} otherwise.
   *  
   *  @param intToTest The number to test.
   *  @return The byte value.
   *  @throws NotAnUnsignedByteException If the value is not an unsigned byte.
   */
  public static byte toByte(int intToTest) {
    if (! isInByteRange(intToTest)) throw new NotAnUnsignedByteException("Not in unsigned byte range " + intToTest);
    return (byte) intToTest;
  }
  
  /**
   *  Converts a given {@link ByteBuffer} to a byte array.
   *  
   *  @param buf The buffer to convert.
   *  @return The resulting byte array.
   */
  public static byte[] toByteArray(ByteBuffer buf) {
    final int pos = buf.position();
    final byte[] bytes = new byte[buf.remaining()];
    buf.get(bytes);
    buf.position(pos);
    return bytes;
  }
  
  /**
   *  A variant of {@link #dump} that works on a {@link ByteBuffer}.
   *  
   *  @param buf The buffer.
   *  @return The formatted string, potentially containing newline characters.
   */
  public static String dump(ByteBuffer buf) {
    return dump(toByteArray(buf));
  }
  
  /**
   *  Dumps the contents of the given byte array to a formatted hex string, using a multi-line,
   *  8 + 8 layout with an ASCII side-note, commonly used in hex editors. <p>
   *  
   *  A typical hex dump resembles the following: <br>
   *  <pre>
   *  {@code
   *  3C 3D 3E 3F 40 41 42 43   44 45 46 47 48 49 4A 4B   <=>?@ABCDEFGHIJK
   *  4C 4D 4E 4F 50 51 52 53   54                        LMNOPQRST
   *  }
   *  </pre>
   *  
   *  @param bytes The byte array.
   *  @return The formatted string, potentially containing newline characters.
   */
  public static String dump(byte[] bytes) {
    final StringBuilder sb = new StringBuilder();
    final StringBuilder byteLine = new StringBuilder();
    final StringBuilder charLine = new StringBuilder();
    for (int i = 0; i < bytes.length; i++) {
      byteLine.append(toHex(bytes[i]).toUpperCase());
      final char ch = (char) bytes[i];
      charLine.append(isPrintable(ch) ? ch : '.');
      
      if (i != bytes.length - 1) {
        if (i % 16 == 15) {
          sb.append(byteLine);
          sb.append("   ");
          sb.append(charLine);
          byteLine.delete(0, byteLine.length());
          charLine.delete(0, charLine.length());
          sb.append('\n');
        } else if (i % 8 == 7) {
          byteLine.append("   ");
        } else {
          byteLine.append(' ');
        }
      }
    }
    
    if (byteLine.length() > 0) {
      sb.append(String.format("%-49s", byteLine));
      sb.append("   ");
      sb.append(charLine);
    }
    return sb.toString();
  }
  
  private static boolean isPrintable(char ch) {
    return ch >= 32 && ch < 127;
  }
  
  /**
   *  Converts a given byte to a pair of hex characters, zero-padded if
   *  the unsigned value is lower than 0x10. The resulting representation
   *  is lower case. (Use {@link String#toUpperCase()} on the resulting
   *  value if necessary.)
   *  
   *  @param b The byte to convert.
   *  @return The hex string.
   */
  public static String toHex(byte b) {
    final int unsignedInt = Byte.toUnsignedInt(b);
    final String str = Integer.toHexString(unsignedInt);
    return unsignedInt < 0x10 ? "0" + str : str;
  }
  
  /**
   *  Converts a varargs array of integers into a byte array, where each of the
   *  integers is assumed to be holding an unsigned byte value.
   *  
   *  @param unsignedBytes The bytes (valid values 0x00—0xFF) to convert.
   *  @return The resulting byte array.
   */
  public static byte[] toByteArray(int... unsignedBytes) {
    final byte[] bytes = new byte[unsignedBytes.length];
    for (int i = 0; i < unsignedBytes.length; i++) {
      bytes[i] = toByte(unsignedBytes[i]);
    }
    return bytes;
  }
  
  /**
   *  Converts a varargs array of integers into a {@link ByteBuffer}, where the integers are
   *  assumed to be holding an unsigned byte value.
   *  
   *  @param unsignedBytes The bytes (valid values 0x00—0xFF) to convert.
   *  @return The resulting {@link ByteBuffer}.
   */
  public static ByteBuffer toByteBuffer(int... unsignedBytes) {
    final byte[] bytes = toByteArray(unsignedBytes);
    return ByteBuffer.wrap(bytes);
  }
}
