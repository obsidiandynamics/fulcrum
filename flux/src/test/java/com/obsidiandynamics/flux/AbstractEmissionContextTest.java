package com.obsidiandynamics.flux;

import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.assertj.core.api.*;
import org.junit.*;

import com.obsidiandynamics.func.*;

public final class AbstractEmissionContextTest {
  private final class TestContext extends AbstractEmissionContext<Integer> {
    @Override
    protected void terminateImpl() {}
  }
  
  @Test
  public void testLimit() {
    final TestContext context = new TestContext();
    context.setLimit(1);
    assertEquals(1, context.getLimit());
    context.decrementLimit();
    assertEquals(0, context.getLimit());
    
    Assertions.assertThatThrownBy(context::decrementLimit)
    .isInstanceOf(IllegalArgumentException.class).hasMessage("No remaining capacity");
    assertFalse(context.isTerminated());
    assertEquals(0, context.getLimit());
  }
  
  @Test
  public void testEmitAndNext_seq01010() {
    final TestContext context = new TestContext();
    context.setLimit(1);
    assertFalse(context.hasNext());
    assertEquals(0, context.size());
    assertEquals(Collections.emptyList(), context.getEventsCopy());
    
    // pops the sole element from a context containing exactly one element, verifying pre and postconditions
    final class Popper {
      int first(TestContext context) {
        assertEquals(1, context.size());
        assertEquals(0, context.remainingCapacity());
        assertTrue(context.hasNext());
        final Integer next = context.next();
        assertNotNull(next);
        assertFalse(context.hasNext());
        assertEquals(0, context.size());
        assertEquals(Collections.emptyList(), context.getEventsCopy());
        assertEquals(1, context.remainingCapacity());
        return next;
      }
    };
    final Popper popper = new Popper();
    
    context.emit(0);
    assertEquals(Collections.singletonList(0), context.getEventsCopy());
    Assertions.assertThat(context.toString()).contains("[0]");
    assertEquals(0, popper.first(context));

    context.emit(1);
    assertEquals(Collections.singletonList(1), context.getEventsCopy());
    assertEquals(1, popper.first(context));
    assertEquals(1, context.remainingCapacity());
  }
  
  @Test
  public void testEmitAndNext_seq012101210() {
    final TestContext context = new TestContext();
    context.setLimit(2);
    
    // pops elements from a context containing one or two elements, verifying pre and postconditions
    final class Popper {
      int first(TestContext context) {
        assertEquals(2, context.size());
        assertEquals(0, context.remainingCapacity());
        assertTrue(context.hasNext());
        final Integer next = context.next();
        assertNotNull(next);
        assertTrue(context.hasNext());
        assertEquals(1, context.size());
        assertEquals(1, context.remainingCapacity());
        return next;
      }
      
      int second(TestContext context) {
        assertEquals(1, context.size());
        assertEquals(1, context.remainingCapacity());
        assertTrue(context.hasNext());
        final Integer next = context.next();
        assertNotNull(next);
        assertFalse(context.hasNext());
        assertEquals(0, context.size());
        assertEquals(Collections.emptyList(), context.getEventsCopy());
        assertEquals(2, context.remainingCapacity());
        return next;
      }
    };
    final Popper popper = new Popper();
    
    context.emit(0);
    context.emit(1);
    assertEquals(Arrays.asList(0, 1), context.getEventsCopy());
    Assertions.assertThat(context.toString()).contains("[0, 1]");
    assertEquals(0, popper.first(context));
    assertEquals(1, popper.second(context));

    context.emit(2);
    context.emit(3);
    assertEquals(Arrays.asList(2, 3), context.getEventsCopy());
    assertEquals(2, popper.first(context));
    assertEquals(3, popper.second(context));
    assertEquals(2, context.remainingCapacity());
  }
  
  @Test
  public void testEmitAndNext_seq0121210() {
    final TestContext context = new TestContext();
    context.setLimit(2);
    
    // pops elements from a context containing one or two elements, verifying pre and postconditions
    final class Popper {
      int first(TestContext context) {
        assertEquals(2, context.size());
        assertEquals(0, context.remainingCapacity());
        assertTrue(context.hasNext());
        final Integer next = context.next();
        assertNotNull(next);
        assertTrue(context.hasNext());
        assertEquals(1, context.size());
        assertEquals(1, context.remainingCapacity());
        return next;
      }
      
      int second(TestContext context) {
        assertEquals(1, context.size());
        assertEquals(1, context.remainingCapacity());
        assertTrue(context.hasNext());
        final Integer next = context.next();
        assertNotNull(next);
        assertFalse(context.hasNext());
        assertEquals(0, context.size());
        assertEquals(2, context.remainingCapacity());
        return next;
      }
    };
    final Popper popper = new Popper();
    
    context.emit(0);
    context.emit(1);
    assertEquals(0, popper.first(context));

    context.emit(2);
    assertEquals(Arrays.asList(1, 2), context.getEventsCopy());
    assertEquals(1, popper.first(context));
    assertEquals(2, popper.second(context));
    assertEquals(2, context.remainingCapacity());
  }
  
  @Test
  public void testEmitAndNext_randomSequence() {
    final TestContext context = new TestContext();
    final int maxRandom = 100;
    final int cycles = 5;
    final int runsPerCycle = 10;
    final AtomicInteger generator = new AtomicInteger();
    
    final class Popper {
      int lastPopped = -1;
      
      void popAndCheck() {
        final int popped = (int) context.next();
        assertEquals(lastPopped + 1, popped);
        lastPopped = popped;
      }
    };
    final Popper popper = new Popper();
    
    for (int cycle = 0; cycle < cycles; cycle++) {
      for (int run = 0; run < runsPerCycle; run++) {
        final int emissions = random(maxRandom);
        for (int j = 0; j < emissions; j++) {
          context.emit(generator.getAndIncrement());
        }
        
        final int pops = random(context.size());
        for (int j = 0; j < pops; j++) {
          popper.popAndCheck();
        }
      }
      
      while (context.hasNext()) {
        popper.popAndCheck();
      }
    }
    
    while (context.hasNext()) {
      popper.popAndCheck();
    }
  }
  
  private static int random(int max) {
    return (int) (max * Math.random());
  }
  
  @Test
  public void testNext_empty() {
    final TestContext context = new TestContext();
    
    Assertions.assertThatThrownBy(context::next).isInstanceOf(NoSuchElementException.class).hasMessage("No more events");
    
    context.emit(0);
    assertEquals(0, (int) context.next());
    
    Assertions.assertThatThrownBy(context::next).isInstanceOf(NoSuchElementException.class).hasMessage("No more events");
  }
  
  @Test
  public void testTerminate_twice() {
    final AtomicInteger invocationCounter = new AtomicInteger();
    final AbstractEmissionContext<Integer> context = new AbstractEmissionContext<Integer>() {
      @Override
      protected void terminateImpl() {
        invocationCounter.incrementAndGet();
      }
    };
    context.terminate();
    assertTrue(context.isTerminated());
    assertEquals(1, invocationCounter.get());
    
    Assertions.assertThatThrownBy(context::terminate)
    .isInstanceOf(IllegalStateException.class).hasMessage("Context has been terminated");
    assertTrue(context.isTerminated());
    assertEquals(1, invocationCounter.get());
  }
  
  @Test
  public void testTerminate_nonEmpty() {
    final TestContext context = new TestContext();
    context.emit(0);
    
    Assertions.assertThatThrownBy(context::terminate)
    .isInstanceOf(IllegalStateException.class).hasMessage("At least one event has already been emitted");
    assertFalse(context.isTerminated());
    
    assertEquals(0, (int) context.next());
    assertFalse(context.hasNext());
    
    context.terminate();
    assertTrue(context.isTerminated());
  }
  
  @Test
  public void testEmit_afterTerminate() {
    final TestContext context = new TestContext();
    context.terminate();
    
    Assertions.assertThatThrownBy(() -> {
      context.emit(0);
    })
    .isInstanceOf(IllegalStateException.class).hasMessage("Context has been terminated");
    assertFalse(context.hasNext());
  }
  
  @Test
  public void testEmit_nullEvent() {
    final TestContext context = new TestContext();
    
    Assertions.assertThatThrownBy(() -> {
      context.emit(null);
    })
    .isInstanceOf(NullArgumentException.class).hasMessage("Event cannot be null");
    assertFalse(context.hasNext());
  }
  
  @Test
  public void testEmit_noRemainingCapacity() {
    final TestContext context = new TestContext();
    context.setLimit(2);
    assertEquals(2, context.remainingCapacity());
    
    context.emit(0);
    context.emit(1);
    assertEquals(0, context.remainingCapacity());
    
    Assertions.assertThatThrownBy(() -> {
      context.emit(2);
    })
    .isInstanceOf(IllegalStateException.class).hasMessage("No remaining capacity");
    assertEquals(0, context.remainingCapacity());
    
    assertEquals(0, (int) context.next());
    assertEquals(1, context.remainingCapacity());
    context.emit(2);
    assertEquals(0, context.remainingCapacity());
    
    assertEquals(1, (int) context.next());
    assertEquals(2, (int) context.next());
    assertEquals(2, context.remainingCapacity());
  }
}
