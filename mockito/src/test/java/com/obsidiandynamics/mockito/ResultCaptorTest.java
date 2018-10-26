package com.obsidiandynamics.mockito;

import static java.util.Arrays.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.*;

import com.obsidiandynamics.func.*;
import com.obsidiandynamics.mockito.ResultCaptor.*;
import com.obsidiandynamics.verifier.*;

import nl.jqno.equalsverifier.*;

public final class ResultCaptorTest {
  private interface ArgsNonVoid {
    String doSomething(String string, int integer, boolean bool);
  }
  
  @Test
  public void testCapturePojo() {
    PojoVerifier.forClass(Capture.class).excludeAccessor("result").excludeAccessor("exception").verify();
  }
  
  @Test
  public void testCaptureEqualsHashCode() {
    EqualsVerifier.forClass(Capture.class).verify();
  }
  
  @Test
  public void testNonNullResponse() {
    final ArgsNonVoid mock = mock(ArgsNonVoid.class);
    final ResultCaptor<Object> captor = ResultCaptor.of(invocation -> {
      final String string = invocation.getArgument(0);
      final Integer integer = invocation.getArgument(1);
      final Boolean bool = invocation.getArgument(2);
      return string + "_" + integer + "_" + bool;
    });
    when(mock.doSomething(any(), anyInt(), anyBoolean())).then(captor);
    
    assertEquals("string_42_true", mock.doSomething("string", 42, true));
    assertEquals("string_42_false", mock.doSomething("string", 42, false));
    
    assertEquals(Capture.result("string_42_true"), captor.get("string", 42, true));
    assertFalse(captor.get("string", 42, true).isException());
    assertEquals(Capture.result("string_42_false"), captor.get("string", 42, false));
    assertFalse(captor.get("string", 42, false).isException());
    assertEquals(2, captor.count());
    
    assertEquals(MapBuilder
                 .init(asList("string", 42, true), Capture.result("string_42_true"))
                 .with(asList("string", 42, false), Capture.result("string_42_false"))
                 .build(),
                 captor.all());
  }
  
  @Test
  public void testNullResponse() {
    final ArgsNonVoid mock = mock(ArgsNonVoid.class);
    final ResultCaptor<Object> captor = ResultCaptor.of(invocation -> null);
    when(mock.doSomething(any(), anyInt(), anyBoolean())).then(captor);
    
    assertNull(mock.doSomething("string", 42, true));
    assertNull(mock.doSomething("string", 42, false));

    assertEquals(Capture.empty(), captor.get("string", 42, true));
    assertEquals(Capture.empty(), captor.get("string", 42, false));
    assertEquals(2, captor.count());
    
    assertEquals(MapBuilder
                 .init(asList("string", 42, true), Capture.empty())
                 .with(asList("string", 42, false), Capture.empty())
                 .build(),
                 captor.all());
  }
  
  private interface ArgsVoid {
    void doSomething(String string);
  }
  
  @Test
  public void testVoid() {
    final ArgsVoid mock = mock(ArgsVoid.class);
    final ResultCaptor<Object> captor = ResultCaptor.of(invocation -> null);
    doAnswer(captor).when(mock).doSomething(any());

    mock.doSomething("string");
    
    assertEquals(Capture.empty(), captor.get("string"));
    assertEquals(1, captor.count());
    assertEquals(Collections.singletonMap(asList("string"), Capture.empty()), captor.all());
  }
  
  @Test
  public void testVoidWithException() {
    final ArgsVoid mock = mock(ArgsVoid.class);
    final ResultCaptor<Object> captor = ResultCaptor.of(invocation -> {
      throw new RuntimeException("simulated");
    });
    doAnswer(captor).when(mock).doSomething(any());

    try {
      mock.doSomething("string");
      fail("Expected exception");
    } catch (RuntimeException e) {
      // expected
    } catch (Throwable e) {
      fail("Unexpected exception " + e);
    }
    
    final Capture<Object> capture = captor.get("string");
    assertNull(capture.result());
    assertTrue(capture.isException());
    assertNotNull(capture.exception());
    assertEquals(RuntimeException.class, capture.exception().getClass());
    assertEquals(1, captor.count());
  }
  
  private interface NoArgsVoid {
    void doSomething();
  }
  
  @Test
  public void testNoArgsVoid() {
    final NoArgsVoid mock = mock(NoArgsVoid.class);
    final ResultCaptor<Object> captor = ResultCaptor.of(invocation -> null);
    doAnswer(captor).when(mock).doSomething();

    mock.doSomething();
    
    assertEquals(Capture.empty(), captor.get());
    assertEquals(1, captor.count());
    assertEquals(Collections.singletonMap(asList(), Capture.empty()), captor.all());
  }
  
  @Test(expected=NoResultForArgsError.class)
  public void testNoResult() {
    ResultCaptor.of(invocation -> null).get(asList("string"), 42, true);
  }
}
