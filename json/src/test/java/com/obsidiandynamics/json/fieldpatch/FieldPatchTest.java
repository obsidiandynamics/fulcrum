package com.obsidiandynamics.json.fieldpatch;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;
import com.obsidiandynamics.func.*;
import com.obsidiandynamics.json.fieldpatch.FieldPatch.*;

import nl.jqno.equalsverifier.*;

public final class FieldPatchTest {
  @Test
  public void testStandardOfNull() {
    final FieldPatch<Object> patch = FieldPatch.ofNull();
    assertNotNull(patch);
    assertNull(patch.get());
    assertSame(patch, FieldPatch.ofNull());
  }
  
  @Test
  public void testStandardOf() {
    final FieldPatch<String> patch = FieldPatch.of("string");
    assertNotNull(patch);
    assertEquals("string", patch.get());
    Assertions.assertToStringOverride(patch);
  }
  
  @Test(expected=NullArgumentException.class)
  public void testStandardOfWithNullArg() {
    FieldPatch.of(null);
  }

  @Test
  public void testStandardNullableOf() {
    final FieldPatch<String> patch = FieldPatch.nullableOf("string");
    assertNotNull(patch);
    assertEquals("string", patch.get());
    Assertions.assertToStringOverride(patch);
  }

  @Test
  public void testStandardNullableOfWithNullArg() {
    final FieldPatch<Object> patch = FieldPatch.nullableOf(null);
    assertNotNull(patch);
    assertNull(patch.get());
    assertSame(patch, FieldPatch.ofNull());
  }
  
  @Test
  public void testStandardEqualsHashCode() {
    EqualsVerifier.forClass(StandardFieldPatch.class).verify();
  }
  
  @Test
  public void testBaseToString() {
    final class CustomPatch implements FieldPatch<String> {
      @Override
      public String get() {
        return "someValue";
      }
    }
    
    assertEquals("CustomPatch [someValue]", new CustomPatch().baseToString());
  }
  
  @Test
  public void testIfExists() {
    final AtomicReference<String> ref = new AtomicReference<>();
    
    // the null scenario
    FieldPatch.ifExists(null, ref::set);
    assertNull(ref.get());
    
    // the non-null scenario
    FieldPatch.ifExists(FieldPatch.of("string"), ref::set);
    assertEquals("string", ref.get());
  }
  
  @Test
  public void testIfExistsWithTransform() {
    final AtomicReference<Integer> ref = new AtomicReference<>();
    
    // the null scenario
    FieldPatch.ifExists(null, ref::set);
    assertNull(ref.get());
    
    // the non-null scenario
    FieldPatch.ifExists(FieldPatch.of("42"), Integer::parseInt, ref::set);
    assertEquals(42, (int) ref.get());
  }
}
