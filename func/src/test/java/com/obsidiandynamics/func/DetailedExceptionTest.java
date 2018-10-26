package com.obsidiandynamics.func;

import static org.junit.Assert.*;

import org.junit.*;

public final class DetailedExceptionTest {
  private static final class DetailedTestException extends Exception implements DetailedException<Object> {
    private static final long serialVersionUID = 1L;

    private final Object detail;
    
    DetailedTestException(Object detail) {
      this.detail = detail;
    }

    @Override
    public Object describeException() {
      return detail;
    }
    
  }
  
  @Test
  public void testDescribe() {
    final DetailedException<Object> e = new DetailedTestException("foo");
    assertNotNull(e.describeException());
  }
}
