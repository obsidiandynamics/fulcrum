package com.obsidiandynamics.constraints;

import static org.junit.Assert.*;

import javax.validation.*;
import javax.validation.constraints.*;

import org.junit.*;
import org.junit.rules.*;

public final class ConstraintsTest {
  public static final class TestNotNull {
    @NotNull
    private final Object value;
    
    @Valid
    private Object child;
    
    TestNotNull() {
      this(null);
    }
    
    TestNotNull(Object value) {
      this.value = value;
    }
    
    TestNotNull withChild(Object child) {
      this.child = child;
      return this;
    }
  }
  
  @Rule
  public final ExpectedException expectedException = ExpectedException.none();
  
  @Test
  public void testNotNullPass() {
    final TestNotNull obj = new TestNotNull("foo");
    assertSame(obj, Constraints.validate(obj));
  }
  
  @Test
  public void testNotNullFail() {
    final TestNotNull obj = new TestNotNull();
    expectedException.expect(ConstraintViolationException.class);
    expectedException.expectMessage("value: must not be null");
    Constraints.validate(obj);
  }
  
  @Test
  public void testNotNullRecursivePass() {
    final TestNotNull obj = new TestNotNull("foo").withChild(new TestNotNull("bar"));
    assertSame(obj, Constraints.validate(obj));
  }
  
  @Test
  public void testNotNullRecursiveFail() {
    final TestNotNull obj = new TestNotNull("foo").withChild(new TestNotNull());
    expectedException.expect(ConstraintViolationException.class);
    expectedException.expectMessage("child.value: must not be null");
    Constraints.validate(obj);
  }
}
