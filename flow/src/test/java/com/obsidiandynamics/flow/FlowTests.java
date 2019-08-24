package com.obsidiandynamics.flow;

import static org.junit.Assert.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

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

  static class ListQuery<T> {
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
      return () -> assertTrue("element " + element + " missing from list " + list, list.contains(element));
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
    
    class DelayedLoop {
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
}
