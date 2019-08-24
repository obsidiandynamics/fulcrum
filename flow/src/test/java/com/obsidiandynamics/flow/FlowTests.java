package com.obsidiandynamics.flow;

import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;

import org.assertj.core.api.*;

import com.obsidiandynamics.await.*;
import com.obsidiandynamics.func.*;
import com.obsidiandynamics.threads.*;

final class FlowTests {
  private FlowTests() {}

  static final class TestTask implements Runnable {
    private final List<Integer> list;
    private final int taskId;

    TestTask(List<Integer> list, int taskId) {
      this.list = list;
      this.taskId = taskId;
    }

    @Override
    public void run() {
      list.add(taskId);
    }

    @Override
    public String toString() {
      return TestTask.class.getSimpleName() + " [taskId=" + taskId + "]";
    }
  }
  
  static List<Integer> increasingListOf(int numElements) {
    final List<Integer> nums = new ArrayList<>(numElements);
    IntStream.range(0, numElements).forEach(nums::add);
    return nums;
  }
  
  static void assertThat(Runnable assertion) {
    assertion.run();
  }

  static final class ListQuery<T> {
    private final List<T> list;

    private ListQuery(List<T> list) {
      this.list = list;
    }

    static <T> ListQuery<T> of(List<T> list) {
      return new ListQuery<T>(list);
    }

    Runnable isSize(int numberOfElements) {
      return () -> assertEquals(numberOfElements, list.size());
    }
    
    Runnable contains(T element) {
      return () -> assertTrue("Element " + element + " missing from list " + list, list.contains(element));
    }
    
    Runnable isOrderedBy(Comparator<T> comparator) {
      final List<T> ordered = transform(l -> Collections.sort(l, comparator)).list;
      return () -> assertEquals(ordered, list);
    }
    
    List<T> list() {
      return list;
    }

    ListQuery<T> transform(Consumer<List<T>> transform) {
      final List<T> copy = new ArrayList<>(list);
      transform.accept(copy);
      return new ListQuery<>(copy);
    }
    
    DelayedLoop delayedBy(int delayMillis) {
      return new DelayedLoop(delayMillis);
    }
    
    final class DelayedLoop {
      private final int delayMillis;

      DelayedLoop(int delayMillis) {
        this.delayMillis = delayMillis;
      }
      
      void forEach(Consumer<T> consumer) {
        list.forEach(t -> {
          if (delayMillis != 0) Threads.sleep(delayMillis); else Thread.yield();
          consumer.accept(t);
        });
      }
    }
  }
  
  static boolean ASSERT_ALL = false;
  static boolean ASSERT_LAST = true;
  
  static void testMultithreadedBeginAndConfirm(Flow flow, 
                                               int tasks, 
                                               ExecutorService executor, 
                                               Timesert wait,
                                               boolean assertOnlyLast) throws InterruptedException {
    final List<Integer> completions = new CopyOnWriteArrayList<>();
    final List<StatefulConfirmation> confirmations = 
        Functions.parallelMapStream(IntStream.range(0, tasks).boxed(), 
                                    taskId -> flow.begin(taskId, new TestTask(completions, taskId)),
                                    executor);
    
    Collections.sort(confirmations, FlowTests::compareConfirmations);
    final List<StatefulConfirmation> orderedConfirmations = new ArrayList<>(confirmations);
    final List<Integer> orderedConfirmationIds = orderedConfirmations.stream()
        .map(confirmation -> (Integer) confirmation.getId())
        .collect(Collectors.toList());
    
    Collections.shuffle(confirmations);
    Functions.parallelMapStream(confirmations.stream(), 
                                confirmation -> {
                                  Threads.sleep(1);
                                  confirmation.confirm();
                                  return null;
                                }, 
                                executor);
    
    // wait for the confirmations to trickle through
    final Integer lastConfirmationId = lastOf(orderedConfirmationIds);
    final Runnable assertion = () -> {
      if (assertOnlyLast) {
        Assertions.assertThat(completions.size()).isGreaterThan(0);
        assertEquals(lastConfirmationId, lastOf(completions));
      } else {
        assertEquals(tasks, completions.size());
      }
    };
    
    if (wait != null) {
      wait.until(assertion);
    } else {
      assertThat(assertion);
    }
    
    if (assertOnlyLast) {
      assertThat(ListQuery.of(completions).isOrderedBy(relativeOrderOf(orderedConfirmationIds)));
    } else {
      Assertions.assertThat(completions).hasSameElementsAs(orderedConfirmationIds);
    }
  }
  
  static <T> T lastOf(List<T> list) {
    return list.get(list.size() - 1);
  }
  
  static int compareConfirmations(StatefulConfirmation c0, StatefulConfirmation c1) {
    if (c0 == c1) return 0;
    
    StatefulConfirmation current = c0;
    while (current != null) {
      if (current == c1) {
        return -1;
      } else {
        current = current.next();
      }
    }
    return 1;
  }
  
  static <T> Comparator<T> relativeOrderOf(List<? extends T> items) {
    return (t0, t1) -> Integer.compare(items.indexOf(t0), items.indexOf(t1));
  }
}
