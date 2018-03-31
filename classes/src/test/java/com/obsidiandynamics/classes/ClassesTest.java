package com.obsidiandynamics.classes;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class ClassesTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Classes.class);
  }

  @Test
  public void testCast() {
    final String s = Classes.cast((Object) "hello");
    assertEquals("hello", s);
  }
  
  @Test
  public void testCompress() {
    final String className = "com.obsidiandynamics.foo.Bar";
    assertEquals(className, Classes.compressPackage(className, 0));
    assertEquals("c.obsidiandynamics.foo.Bar", Classes.compressPackage(className, 1));
    assertEquals("c.o.f.Bar", Classes.compressPackage(className, 3));
    assertEquals("c.o.f.Bar", Classes.compressPackage(className, 4));
  }

  @Test
  public void testCompressDefaultPackage() {
    final String className = "Bar";
    assertEquals(className, Classes.compressPackage(className, 0));
    assertEquals(className, Classes.compressPackage(className, 1));
  }
}
