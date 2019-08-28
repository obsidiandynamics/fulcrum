package com.obsidiandynamics.func;

import static org.junit.Assert.*;

import java.util.function.*;

import org.junit.*;

public final class CheckedBooleanSupplierTest {
  @Test
  public void testToChecked() {
    final CheckedBooleanSupplier<RuntimeException> s = CheckedBooleanSupplier.toChecked(() -> true);
    assertTrue(s.getAsBoolean());
  }

  @Test
  public void testToUnchecked() {
    final BooleanSupplier s = CheckedBooleanSupplier.toUnchecked(() -> true);
    assertTrue(s.getAsBoolean());
  }
}
