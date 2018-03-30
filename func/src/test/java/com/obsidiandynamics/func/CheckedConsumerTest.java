package com.obsidiandynamics.func;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.*;

import org.junit.*;

public final class CheckedConsumerTest {
  @Test
  public void testAccept() throws Exception {
    final AtomicReference<String> consumed0 = new AtomicReference<>();
    final AtomicReference<String> consumed1 = new AtomicReference<>();
    final ThrowingConsumer<String> c = consumed0::set;
    final CheckedConsumer<String, Exception> cc = c.andThen(consumed1::set);
    cc.accept("test");
    
    assertEquals("test", consumed0.get());
    assertEquals("test", consumed1.get());
  }
  
  @Test
  public void testNop() {
    CheckedConsumer.nop(null);
  }
  
  @Test
  public void testWrap() {
    final AtomicReference<String> consumed = new AtomicReference<>();
    final CheckedConsumer<String, RuntimeException> c = CheckedConsumer.wrap(consumed::set);
    c.accept("test");
    assertEquals("test", consumed.get());
  }
}
