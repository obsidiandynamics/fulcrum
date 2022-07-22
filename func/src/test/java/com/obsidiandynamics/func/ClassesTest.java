package com.obsidiandynamics.func;

import static org.junit.Assert.*;

import java.math.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

public final class ClassesTest {
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Classes.class);
  }

  @Test
  public void testCast() {
    final Object obj = "hello";
    final String s = Classes.cast(obj);
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
  
  @Test
  public void testCoerceNull() {
    final AtomicBoolean parsed = new AtomicBoolean();
    final Integer intValue = Classes.coerce(null, Integer.class, value -> {
      parsed.set(true);
      return Integer.parseInt(String.valueOf(value));
    });
    assertNull(intValue);
    assertFalse(parsed.get());
  }
  
  static <T, R> Function<T, R> uncheck(CheckedFunction<? super T, ? extends R, ? extends RuntimeException> checkedFunction) {
    return checkedFunction::apply;
  }
  
  @Test
  public void testCoerceSameType() {
    final AtomicBoolean parsed = new AtomicBoolean();
    final Integer intValue = Classes.coerce(42, Integer.class, value -> {
      parsed.set(true);
      return Integer.parseInt(String.valueOf(value));
    });
    assertEquals(42, intValue.intValue());
    assertFalse(parsed.get());
  }
  
  @Test
  public void testCoerceDifferentType() {
    final AtomicBoolean parsed = new AtomicBoolean();
    final Integer intValue = Classes.coerce("42", Integer.class, value -> {
      parsed.set(true);
      return Integer.parseInt(String.valueOf(value));
    });
    assertEquals(42, intValue.intValue());
    assertTrue(parsed.get());
  }
  
  @Test
  public void testCoerceExamples() {
    {
      // coercion takes place through string parsing
      final Object value = "42";
      final Integer intValue = Classes.coerce(value, Integer.class, v -> Integer.parseInt(String.valueOf(v)));
      assertEquals(42, intValue.intValue());
    }
    {
      // similarly, because we force to a string first, the following also works
      final Object value = new BigDecimal("42");
      final Integer intValue = Classes.coerce(value, Integer.class, v -> Integer.parseInt(String.valueOf(v)));
      assertEquals(42, intValue.intValue());
    }
    {
      // coercion here is merely a result of type casting
      final Object value = 42;
      final Integer intValue = Classes.coerce(value, Integer.class, v -> Integer.parseInt(String.valueOf(v)));
      assertEquals(42, intValue.intValue());
    }
  }
}
