package com.obsidiandynamics.threads;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public final class Parallel implements Runnable {
  private static final boolean BLOCKING = true;
  private static final boolean NON_BLOCKING = false;
  
  private final CountDownLatch latch;
  private final CyclicBarrier barrier;
  
  private Parallel(CountDownLatch latch, CyclicBarrier barrier) {
    this.latch = latch;
    this.barrier = barrier;
  }
  
  public static Parallel blocking(int threads, IntConsumer r) {
    return create(threads, BLOCKING, r);
  }
  
  public static Parallel nonBlocking(int threads, IntConsumer r) {
    return create(threads, NON_BLOCKING, r);
  }
  
  private static Parallel create(int threads, boolean blocking, IntConsumer r) {
    final CountDownLatch latch = blocking ? new CountDownLatch(threads) : null;
    final CyclicBarrier barrier = new CyclicBarrier(threads + 1);
    final String threadNameFormat = "ParRunner-%0" + numDigits(threads) + "d";
    for (int i = 0; i < threads; i++) {
      final int _i = i;
      final Thread t = new Thread(() ->  {
        Threads.await(barrier);
        try {
          r.accept(_i);
        } finally {
          if (latch != null) latch.countDown();
        }
      }, String.format(threadNameFormat, i));
      t.start();
    }
    return new Parallel(latch, barrier);
  }
  
  private static int numDigits(int num) {
    return String.valueOf(num).length();
  }
  
  public static <T> Parallel blockingSlice(List<T> list, int threads, Consumer<List<T>> task) {
    return slice(list, threads, BLOCKING, task);
  }
  
  public static <T> Parallel nonBlockingSlice(List<T> list, int threads, Consumer<List<T>> task) {
    return slice(list, threads, NON_BLOCKING, task);
  }
  
  static <T> List<List<T>> split(List<T> list, int ways) {
    if (ways < 1 || ways > list.size()) {
      throw new IllegalArgumentException("Number of ways must be between 1 and " + list.size() + " (inclusive)");
    }
    
    final List<List<T>> lists = new ArrayList<>(ways);
    int pos = 0;
    for (int i = 0; i < ways; i++) {
      final int remaining = ways - i;
      final int len = (list.size() - pos) / remaining;
      lists.add(list.subList(pos, pos + len));
      pos += len;
    }
    return lists;
  }
  
  private static <T> Parallel slice(List<T> list, int threads, boolean blocking, Consumer<List<T>> task) {
    final int actualThreads = Math.min(threads, list.size());
    final List<List<T>> lists = split(list, actualThreads);
    
    return create(actualThreads, blocking, i -> {
      final List<T> sublist = lists.get(i);
      task.accept(sublist);
    });
  }
  
  @Override
  public void run() {
    Threads.await(barrier);
    if (latch != null) Threads.await(latch);
  }
}