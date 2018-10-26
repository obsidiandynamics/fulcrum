package com.obsidiandynamics.func.fsm;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;
import org.junit.rules.*;

import nl.jqno.equalsverifier.*;
import pl.pojo.tester.internal.assertion.tostring.*;

public final class EnumOrderTest {
  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  private enum TestEnum {
    A, B, C, D
  }

  @Test
  public void testEqualsHashCode() {
    EqualsVerifier.forClass(EnumOrder.class).verify();
  }

  @Test
  public void testCaptureInsufficientElements() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Order omits one or more required enums");
    EnumOrder.capture(TestEnum.class, TestEnum.A, TestEnum.B);
  }

  @Test
  public void testCaptureDuplicateElements() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("B is not among [A, A, C, D]");
    EnumOrder.capture(TestEnum.class, TestEnum.A, TestEnum.A, TestEnum.C, TestEnum.D);
  }

  @Test
  public void testCaptureReversed() {
    final EnumOrder<TestEnum> order = EnumOrder.capture(TestEnum.class, TestEnum.D, TestEnum.B, TestEnum.A, TestEnum.C);
    assertEquals(2, order.of(TestEnum.A));
    assertEquals(1, order.of(TestEnum.B));
    assertEquals(3, order.of(TestEnum.C));
    assertEquals(0, order.of(TestEnum.D));

    assertEquals(asList(TestEnum.D, TestEnum.B, TestEnum.A, TestEnum.C), asList(order.ordered()));

    final Iterator<TestEnum> iterator = order.iterator();
    assertEquals(TestEnum.D, iterator.next());
    assertEquals(TestEnum.B, iterator.next());
    assertEquals(TestEnum.A, iterator.next());
    assertEquals(TestEnum.C, iterator.next());
    assertFalse(iterator.hasNext());

    final Comparator<TestEnum> comparator = order.comparator();
    assertEquals(-1, comparator.compare(TestEnum.D, TestEnum.A));

    assertEquals(TestEnum.D, order.first());
    assertEquals(TestEnum.C, order.last());

    final ToStringAssertions toStringAssertions = new ToStringAssertions(order);
    toStringAssertions.contains("enumType", TestEnum.class);
    toStringAssertions.contains("order", Arrays.asList(2, 1, 3, 0));
  }

  @Test
  public void testLowestEmpty() {
    final EnumOrder<TestEnum> order = EnumOrder.capture(TestEnum.class, TestEnum.D, TestEnum.B, TestEnum.A, TestEnum.C);

    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("No elements to search");
    order.lowest(Collections.emptyList());
  }

  @Test
  public void testLowest() {
    final EnumOrder<TestEnum> order = EnumOrder.capture(TestEnum.class, TestEnum.D, TestEnum.B, TestEnum.A, TestEnum.C);
    assertEquals(TestEnum.D, order.lowest(asList(TestEnum.D, TestEnum.B, TestEnum.C)));
  }

  @Test
  public void testHighestEmpty() {
    final EnumOrder<TestEnum> order = EnumOrder.capture(TestEnum.class, TestEnum.D, TestEnum.B, TestEnum.A, TestEnum.C);

    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("No elements to search");
    order.highest(Collections.emptyList());
  }

  @Test
  public void testHighest() {
    final EnumOrder<TestEnum> order = EnumOrder.capture(TestEnum.class, TestEnum.D, TestEnum.B, TestEnum.A, TestEnum.C);
    assertEquals(TestEnum.C, order.highest(asList(TestEnum.D, TestEnum.C, TestEnum.A)));
  }

  @Test
  public void testIndexOfMissing() {
    assertEquals(-1, EnumOrder.indexOf(TestEnum.A, new TestEnum[] { TestEnum.B, TestEnum.C }));
  }

  @Test
  public void testIndexOfFound() {
    assertEquals(1, EnumOrder.indexOf(TestEnum.B, TestEnum.values()));
  }

  @Test
  public void testIndexOfMandatoryMissing() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("A is not among [B, C]");
    assertEquals(-1, EnumOrder.indexOfMandatory(TestEnum.A, new TestEnum[] { TestEnum.B, TestEnum.C }));
  }

  @Test
  public void testIndexOfMandatoryFound() {
    assertEquals(1, EnumOrder.indexOfMandatory(TestEnum.B, TestEnum.values()));
  }
}
