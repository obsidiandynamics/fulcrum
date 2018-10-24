package com.obsidiandynamics.format;

import static org.junit.Assert.*;

import java.nio.*;

import org.junit.*;
import org.junit.runners.*;

import com.obsidiandynamics.assertion.*;
import com.obsidiandynamics.format.Binary.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class BinaryTest {
  private static final boolean PRINT_DUMPS = false;
  
  private static void printDump(String dump) {
    if (PRINT_DUMPS) System.out.println("---\n" + dump + "\n---");
  }
  
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Binary.class);
  }
  
  @Test
  public void testInByteRange() {
    assertFalse(Binary.isInByteRange(-0x01));
    assertTrue(Binary.isInByteRange(0x00));
    assertTrue(Binary.isInByteRange(0x01));
    assertTrue(Binary.isInByteRange(0x10));
    assertTrue(Binary.isInByteRange(0xFF));
    assertFalse(Binary.isInByteRange(0x100));
  }
  
  @Test
  public void testToBytePass() {
    assertEquals((byte) 0x00, Binary.toByte(0x00));
    assertEquals((byte) 0x01, Binary.toByte(0x01));
    assertEquals((byte) 0x10, Binary.toByte(0x10));
    assertEquals((byte) 0xFF, Binary.toByte(0xFF));
  }
  
  @Test(expected=NotAnUnsignedByteException.class)
  public void testToByteFailTooLow() {
    Binary.toByte(-0x01);
  }
  
  @Test(expected=NotAnUnsignedByteException.class)
  public void testToByteFailTooHigh() {
    Binary.toByte(0x100);
  }

  @Test
  public void testBufferToByteArray() {
    final byte[] orig = { (byte) 0x00, (byte) 0x01 };
    final ByteBuffer buf = ByteBuffer.wrap(orig);
    
    final byte[] bytes = Binary.toByteArray(buf);
    assertArrayEquals(orig, bytes);
    assertEquals(0, buf.position());
  }
  
  private static String pad(String str) {
    return String.format("%-52s", str);
  }
  
  @Test
  public void testDumpBlank() {
    final byte[] bytes = new byte[0];
    
    final String out = Binary.dump(bytes);
    assertEquals("", out);
  }
  
  @Test
  public void testDumpUnprintable() {
    final byte[] bytes = { (byte) 0x00, (byte) 0x01, (byte) 0xFF };
    final String out = Binary.dump(bytes);
    printDump(out);
    assertEquals(pad("00 01 FF") + "...", out);
  }
  
  @Test
  public void testDumpTiny() {
    final byte[] bytes = new byte[1];
    final int initial = 30;
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = Binary.toByte(i + initial);
    }
    
    final String out = Binary.dump(bytes);
    printDump(out);
    assertEquals(pad("1E") + ".", out);
  }
  
  @Test
  public void testDumpSmall() {
    final byte[] bytes = new byte[9];
    final int initial = 40;
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = Binary.toByte(i + initial);
    }
    
    final String out = Binary.dump(bytes);
    printDump(out);
    assertEquals(pad("28 29 2A 2B 2C 2D 2E 2F   30") + "()*+,-./0", out);
  }
  
  @Test
  public void testDumpMedium() {
    final byte[] bytes = new byte[17];
    final int initial = 50;
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = Binary.toByte(i + initial);
    }
    
    final String out = Binary.dump(bytes);
    printDump(out);
    assertEquals(pad("32 33 34 35 36 37 38 39   3A 3B 3C 3D 3E 3F 40 41") + "23456789:;<=>?@A" + "\n" + 
                 pad("42") + "B", out);
  }
  
  @Test
  public void testDumpLarge() {
    final byte[] bytes = new byte[25];
    final int initial = 60;
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = Binary.toByte(i + initial);
    }
    
    final String out = Binary.dump(bytes);
    printDump(out);
    assertEquals(pad("3C 3D 3E 3F 40 41 42 43   44 45 46 47 48 49 4A 4B") + "<=>?@ABCDEFGHIJK" + "\n" +
                 pad("4C 4D 4E 4F 50 51 52 53   54") + "LMNOPQRST", out);
  }
  
  @Test
  public void testDumpByteBuffer() {
    final byte[] bytes = { (byte) 0x00, (byte) 0x01 };
    assertEquals(Binary.dump(bytes), Binary.dump(ByteBuffer.wrap(bytes)));
  }
  
  @Test
  public void testToHex() {
    assertEquals("00", Binary.toHex((byte) 0x00));
    assertEquals("0a", Binary.toHex((byte) 0x0A));
    assertEquals("10", Binary.toHex((byte) 0x10));
    assertEquals("ff", Binary.toHex((byte) 0xFF));
  }
  
  @Test
  public void testToHexArray() {
    assertEquals("00", Binary.toHex(Binary.toByteArray(0x00)));
    assertEquals("10", Binary.toHex(Binary.toByteArray(0x10)));
    assertEquals("ff", Binary.toHex(Binary.toByteArray(0xFF)));
    assertEquals("0010ff", Binary.toHex(Binary.toByteArray(0x00, 0x10, 0xFF)));
  }
  
  @Test
  public void testToByteArray() {
    final byte[] expected = { (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0xFF };
    assertArrayEquals(expected, Binary.toByteArray(0x00, 0x01, 0x10, 0xFF));
  }
  
  @Test(expected=NotAnUnsignedByteException.class)
  public void testToByteArrayFail() {
    Binary.toByteArray(0x100);
  }
  
  @Test
  public void testToByteBuffer() {
    final byte[] expected = { (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0xFF };
    final ByteBuffer buf = Binary.toByteBuffer(0x00, 0x01, 0x10, 0xFF);
    assertEquals(ByteBuffer.wrap(expected), buf);
  }
}
