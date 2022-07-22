package com.obsidiandynamics.select;

import static com.obsidiandynamics.select.Select.*;
import static java.util.function.Predicate.*;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.*;

import org.junit.*;

import com.obsidiandynamics.func.*;

public final class SelectTest {
  private static class Once<T> extends AtomicReference<T> {
    private static final long serialVersionUID = 1L;

    void assign(T newValue) {
      assertNull(get());
      super.set(newValue);
    }
  }

  @Test
  public void testWhen() {
    final Once<String> branch = new Once<>();
    Select.from("bar")
    .whenNull().then(() -> branch.assign("null"))
    .when(isEqual("foo")).then(obj -> branch.assign("foo"))
    .when(isEqual("bar")).then(obj -> branch.assign("bar"))
    .otherwise(obj -> branch.assign("otherwise"));

    assertEquals("bar", branch.get());
  }

  @Test
  public void testWhenChecked() {
    final Once<String> branch = new Once<>();
    Select.from("bar").checked()
    .whenNull().then(() -> branch.assign("null"))
    .when(isEqual("foo")).then(obj -> branch.assign("foo"))
    .when(isEqual("bar")).then(obj -> branch.assign("bar"))
    .otherwise(obj -> branch.assign("otherwise"));

    assertEquals("bar", branch.get());
  }
  
  private static class TestCheckedExceptionFoo extends Exception {
    private static final long serialVersionUID = 1L;
  }
  
  private static class TestCheckedExceptionBar extends Exception {
    private static final long serialVersionUID = 1L;
  }
  
  private static class TestRuntimeExceptionFoo extends RuntimeException {
    private static final long serialVersionUID = 1L;
  }
  
  private static class TestRuntimeExceptionBar extends RuntimeException {
    private static final long serialVersionUID = 1L;
  }
  
  @Test(expected=TestRuntimeExceptionBar.class)
  public void testWhenWithThrow() {
    Select.from("bar")
    .whenNull().thenThrow(TestRuntimeExceptionFoo::new)
    .when(isEqual("foo")).thenThrow(TestRuntimeExceptionFoo::new)
    .when(isEqual("bar")).thenThrow(TestRuntimeExceptionBar::new)
    .when(isEqual("baz")).then(CheckedConsumer::nop)
    .otherwiseThrow(TestRuntimeExceptionFoo::new);
  }
  
  @Test(expected=TestRuntimeExceptionFoo.class)
  public void testWhenNullWithThrow() {
    Select.from(null)
    .when(isEqual("bar")).thenThrow(TestRuntimeExceptionBar::new)
    .whenNull().thenThrow(TestRuntimeExceptionFoo::new)
    .otherwise(obj -> {});
  }
  
  @Test(expected=TestRuntimeExceptionBar.class)
  public void testOtherwiseWithThrow() {
    Select.from(null)
    .when(isEqual("foo")).thenThrow(TestRuntimeExceptionFoo::new)
    .otherwiseThrow(TestRuntimeExceptionBar::new);
  }
  
  @Test(expected=TestCheckedExceptionBar.class)
  public void testWhenCheckedWithThrow() throws TestCheckedExceptionBar, TestCheckedExceptionFoo {
    Select.from("bar").checked()
    .whenNull().thenThrow(TestCheckedExceptionFoo::new)
    .when(isEqual("foo")).thenThrow(TestCheckedExceptionFoo::new)
    .when(isEqual("bar")).thenThrow(TestCheckedExceptionBar::new)
    .when(isEqual("baz")).then(CheckedConsumer::nop)
    .otherwiseThrow(TestCheckedExceptionFoo::new);
  }
  
  @Test(expected=TestCheckedExceptionFoo.class)
  public void testWhenCheckedNullWithThrow() throws TestCheckedExceptionBar, TestCheckedExceptionFoo {
    Select.from(null).checked()
    .when(isEqual("bar")).thenThrow(TestCheckedExceptionBar::new)
    .whenNull().thenThrow(TestCheckedExceptionFoo::new)
    .otherwise(obj -> {});
  }
  
  @Test(expected=TestCheckedExceptionBar.class)
  public void testOtherwiseCheckedWithThrow() throws TestCheckedExceptionBar, TestCheckedExceptionFoo {
    Select.from(null).checked()
    .when(isEqual("foo")).thenThrow(TestCheckedExceptionFoo::new)
    .otherwiseThrow(TestCheckedExceptionBar::new);
  }

  @Test
  public void testOtherwise() {
    final Once<String> branch = new Once<>();
    Select.from("something_else")
    .when(isEqual("foo")).then(obj -> branch.assign("foo"))
    .when(isEqual("bar")).then(obj -> branch.assign("bar"))
    .otherwise(obj -> branch.assign("otherwise"))
    .otherwise(obj -> branch.assign("otherwise_2"));

    assertEquals("otherwise", branch.get());
  }

  @Test
  public void testOtherwiseChecked() {
    final Once<String> branch = new Once<>();
    Select.from("something_else").checked()
    .when(isEqual("foo")).then(obj -> branch.assign("foo"))
    .when(isEqual("bar")).then(obj -> branch.assign("bar"))
    .otherwise(obj -> branch.assign("otherwise"))
    .otherwise(obj -> branch.assign("otherwise_2"));

    assertEquals("otherwise", branch.get());
  }

  @Test
  public void testNotEquals() {
    final Once<String> branch = new Once<>();
    Select.from("bar")
    .whenNull().then(() -> branch.assign("null"))
    .when(not(isEqual("bar"))).then(obj -> branch.assign("not_bar"))
    .when(not(isEqual("foo"))).then(obj -> branch.assign("not_foo"))
    .otherwise(obj -> branch.assign("otherwise"));

    assertEquals("not_foo", branch.get());
  }

  @Test
  public void testNotEqualsExceptionOtherwise() {
    final Once<String> branch = new Once<>();
    Select.from("bar")
    .whenNull().then(() -> branch.assign("null"))
    .when(not(isEqual("bar"))).then(obj -> branch.assign("not_bar"))
    .when(not(isEqual("foo"))).then(obj -> branch.assign("not_foo"))
    .otherwiseThrow(IllegalStateException::new);

    assertEquals("not_foo", branch.get());
  }

  @Test
  public void testNotEqualsChecked() {
    final Once<String> branch = new Once<>();
    Select.from("bar").checked()
    .whenNull().then(() -> branch.assign("null"))
    .when(not(isEqual("bar"))).then(obj -> branch.assign("not_bar"))
    .when(not(isEqual("foo"))).then(obj -> branch.assign("not_foo"))
    .otherwise(obj -> branch.assign("otherwise"));

    assertEquals("not_foo", branch.get());
  }

  @Test
  public void testNotEqualsCheckedOtherwiseThrow() {
    final Once<String> branch = new Once<>();
    Select.from("bar").checked()
    .whenNull().then(() -> branch.assign("null"))
    .when(not(isEqual("bar"))).then(obj -> branch.assign("not_bar"))
    .when(not(isEqual("foo"))).then(obj -> branch.assign("not_foo"))
    .otherwiseThrow(IllegalStateException::new);

    assertEquals("not_foo", branch.get());
  }

  @Test
  public void testNotNull() {
    final Once<String> branch = new Once<>();
    Select.from("bar")
    .whenNull().then(() -> branch.assign("null"))
    .when(isEqual("foo")).then(obj -> branch.assign("foo"))
    .when(isNotNull()).then(obj -> branch.assign("not_null"))
    .otherwise(obj -> branch.assign("otherwise"));

    assertEquals("not_null", branch.get());
  }

  @Test
  public void testNotNullChecked() {
    final Once<String> branch = new Once<>();
    Select.from("bar").checked()
    .whenNull().then(() -> branch.assign("null"))
    .when(isEqual("foo")).then(obj -> branch.assign("foo"))
    .when(isNotNull()).then(obj -> branch.assign("not_null"))
    .otherwise(obj -> branch.assign("otherwise"));

    assertEquals("not_null", branch.get());
  }

  @Test
  public void testNull() {
    final Once<String> branch = new Once<>();
    Select.from(null)
    .when(isEqual("foo")).then(obj -> branch.assign("foo"))
    .when(isEqual("bar")).then(obj -> branch.assign("bar"))
    .whenNull().then(() -> branch.assign("null"))
    .otherwise(obj -> branch.assign("otherwise"));

    assertEquals("null", branch.get());
  }

  @Test
  public void testNullChecked() {
    final Once<String> branch = new Once<>();
    Select.from(null).checked()
    .when(isEqual("foo")).then(obj -> branch.assign("foo"))
    .when(isEqual("bar")).then(obj -> branch.assign("bar"))
    .whenNull().then(() -> branch.assign("null"))
    .otherwise(obj -> branch.assign("otherwise"));

    assertEquals("null", branch.get());
  }

  @Test
  public void testInstanceOf() {
    final Once<String> branch = new Once<>();
    Select.from(5L)
    .whenNull().then(() -> branch.assign("null"))
    .when(isEqual((Object) "foo")).then(obj -> branch.assign("foo"))
    .whenInstanceOf(int.class).then(obj -> branch.assign("int"))
    .whenInstanceOf(Integer.class).then(obj -> branch.assign("Integer"))
    .whenInstanceOf(long.class).then(obj -> branch.assign("long"))
    .whenInstanceOf(Long.class).then(obj -> branch.assign("Long"))
    .whenInstanceOf(Number.class).then(obj -> branch.assign("Number"))
    .otherwise(obj -> branch.assign("otherwise"));

    assertEquals("Long", branch.get());
  }

  @Test
  public void testInstanceOfChecked() {
    final Once<String> branch = new Once<>();
    Select.from(5L).checked()
    .whenNull().then(() -> branch.assign("null"))
    .when(isEqual((Object) "foo")).then(obj -> branch.assign("foo"))
    .whenInstanceOf(int.class).then(obj -> branch.assign("int"))
    .whenInstanceOf(Integer.class).then(obj -> branch.assign("Integer"))
    .whenInstanceOf(long.class).then(obj -> branch.assign("long"))
    .whenInstanceOf(Long.class).then(obj -> branch.assign("Long"))
    .whenInstanceOf(Number.class).then(obj -> branch.assign("Number"))
    .otherwise(obj -> branch.assign("otherwise"));

    assertEquals("Long", branch.get());
  }

  @Test
  public void testTransform() {
    final Once<String> branch = new Once<>();
    Select.from("5")
    .whenNull().then(() -> branch.assign("null"))
    .when(isEqual("4")).transform(Integer::parseInt).then(obj -> branch.assign("4"))
    .when(isEqual("5")).transform(Integer::parseInt).then(obj -> {
      assertEquals(Integer.class, obj.getClass());
      branch.assign("5");
    })
    .otherwise(obj -> branch.assign("otherwise"));

    assertEquals("5", branch.get());
  }

  @Test
  public void testTransformChecked() {
    final Once<String> branch = new Once<>();
    Select.from("5").checked()
    .whenNull().then(() -> branch.assign("null"))
    .when(isEqual("4")).transform(Integer::parseInt).then(obj -> branch.assign("4"))
    .when(isEqual("5")).transform(Integer::parseInt).then(obj -> {
      assertEquals(Integer.class, obj.getClass());
      branch.assign("5");
    })
    .otherwise(obj -> branch.assign("otherwise"));

    assertEquals("5", branch.get());
  }

  @Test
  public void testReturn() {
    final String retVal = Select.returning(String.class).from(5L)
        .whenNull().thenReturn(() -> "was null")
        .when(isEqual(1L)).thenReturn(obj -> "was one")
        .when(isEqual(5L)).thenReturn(obj -> "was five")
        .otherwiseReturn(obj -> "was something else")
        .getReturn();

    assertEquals("was five", retVal);
  }

  @Test
  public void testReturnChecked() {
    final String retVal = Select.returning(String.class).from(5L).checked()
        .whenNull().thenReturn(() -> "was null")
        .when(isEqual(1L)).thenReturn(obj -> "was one")
        .when(isEqual(5L)).thenReturn(obj -> "was five")
        .otherwiseReturn(obj -> "was something else")
        .getReturn();

    assertEquals("was five", retVal);
  }

  @Test
  public void testReturnNull() {
    final String retVal = Select.<String>returning().from(10L)
        .whenNull().thenReturn(() -> "was null")
        .when(isEqual(1L)).thenReturn(obj -> "was one")
        .when(isEqual(5L)).thenReturn(obj -> "was five")
        .getReturn();

    assertNull(retVal);
  }

  @Test
  public void testReturnNullChecked() {
    final String retVal = Select.<String>returning().from(10L).checked()
        .whenNull().thenReturn(() -> "was null")
        .when(isEqual(1L)).thenReturn(obj -> "was one")
        .when(isEqual(5L)).thenReturn(obj -> "was five")
        .getReturn();

    assertNull(retVal);
  }
}
