package com.obsidiandynamics.flow;

import static com.obsidiandynamics.flow.FlowTests.*;
import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import com.obsidiandynamics.await.*;
import com.obsidiandynamics.junit.*;
import com.obsidiandynamics.threads.*;

@RunWith(Parameterized.class)
public final class ThreadedFlowTest {
  @Parameterized.Parameters
  public static List<Object[]> data() {
    return TestCycle.timesQuietly(1);
  }
  
  private static final int EXECUTOR_THREADS = Math.min(Math.max(8, Runtime.getRuntime().availableProcessors()), 32);
  
  @ClassRule
  public static final ExecutorProp executorProp = new ExecutorProp(EXECUTOR_THREADS, Executors::newWorkStealingPool);
  
  private final Timesert wait = Timesert.wait(10_000);

  private ThreadedFlow flow;

  @After
  public void after() {
    if (flow != null) flow.terminate().joinSilently();
  }

  private void createFlow(FiringStrategy.Factory firingStrategyFactory) {
    flow = new ThreadedFlow(firingStrategyFactory);
  }
  
  @Test
  public void testStrict_multithreaded() throws InterruptedException {
    createFlow(StrictFiringStrategy::new);
    final int tasks = 100;
    FlowTests.testMultithreadedBeginAndConfirm(flow, tasks, executorProp.getExecutor(), wait, FlowTests.ASSERT_ALL);
  }

  @Test
  public void testStrict_noComplete() {
    createFlow(StrictFiringStrategy::new);
    final int runs = 10;
    final List<Integer> dispatched = new CopyOnWriteArrayList<>();

    for (int i = 0; i < runs; i++) {
      flow.begin(i, new TestTask(dispatched, i));
      flow.begin(i, new TestTask(dispatched, i));
    }
    
    for (int i = 0; i < runs; i++) {
      flow.getPendingConfirmations().get(i).confirm();
    }

    Threads.sleep(10);
    assertEquals(0, dispatched.size());
    assertEquals(runs, flow.getPendingConfirmations().size());
  }

  @Test
  public void testStrict_increasing() {
    createFlow(StrictFiringStrategy::new);
    final int runs = 100;
    final List<Integer> expected = increasingListOf(runs);
    final List<Integer> dispatched = new CopyOnWriteArrayList<>();
    final List<StatefulConfirmation> cons = new ArrayList<>(runs);

    Threads.sleep(10); // covers the idle wait branch of StrictFiringStrategy.cycle()
    expected.forEach(i -> cons.add(flow.begin(i, new TestTask(dispatched, i))));
    
    cons.forEach(a -> a.confirm());
    wait.until(ListQuery.of(dispatched).isSize(runs));
    assertEquals(expected, dispatched);
    assertEquals(0, flow.getPendingConfirmations().size());
  }

  @Test
  public void testStrict_increasingDoublePending() {
    createFlow(StrictFiringStrategy::new);
    final int runs = 100;
    final List<Integer> expected = increasingListOf(runs);
    final List<Integer> dispatched = new CopyOnWriteArrayList<>();
    final List<StatefulConfirmation> cons = new ArrayList<>(runs);

    expected.forEach(i -> {
      final StatefulConfirmation c0 = flow.begin(i, new TestTask(dispatched, i));
      cons.add(c0);
      assertEquals(1, c0.getPendingCount());
      final StatefulConfirmation c1 = flow.begin(i, new TestTask(dispatched, i));
      assertSame(c0, c1);
      assertEquals(2, c0.getPendingCount());
    });
    
    cons.forEach(a -> a.confirm());
    
    // single pass shouldn't lead to any completions
    assertThat(ListQuery.of(dispatched).isSize(0));
    
    cons.forEach(a -> a.confirm());

    wait.until(ListQuery.of(dispatched).isSize(runs));
    assertEquals(expected, dispatched);
    assertEquals(0, flow.getPendingConfirmations().size());
  }

  @Test
  public void testStrict_decreasing() {
    createFlow(StrictFiringStrategy::new);
    final int runs = 100;
    final List<Integer> expected = increasingListOf(runs);
    final List<Integer> dispatched = new CopyOnWriteArrayList<>();
    final List<StatefulConfirmation> cons = new ArrayList<>(runs);

    expected.forEach(i -> cons.add(flow.begin(i, new TestTask(dispatched, i))));
    ListQuery.of(cons).transform(Collections::reverse).list().forEach(a -> a.confirm());

    wait.until(ListQuery.of(dispatched).isSize(runs));
    assertEquals(expected, dispatched);
    assertEquals(0, flow.getPendingConfirmations().size());
  }

  @Test
  public void testStrict_random() {
    createFlow(StrictFiringStrategy::new);
    final int runs = 100;
    final List<Integer> expected = increasingListOf(runs);
    final List<Integer> dispatched = new CopyOnWriteArrayList<>();
    final List<StatefulConfirmation> cons = new ArrayList<>(runs);

    expected.forEach(i -> cons.add(flow.begin(i, new TestTask(dispatched, i))));
    ListQuery.of(cons).transform(Collections::shuffle).list().forEach(a -> a.confirm());

    wait.until(ListQuery.of(dispatched).isSize(runs));
    assertEquals(expected, dispatched);
    assertEquals(0, flow.getPendingConfirmations().size());
  }
  
  @Test
  public void testLazy_multithreaded() throws InterruptedException {
    createFlow(LazyFiringStrategy::new);
    final int tasks = 100;
    FlowTests.testMultithreadedBeginAndConfirm(flow, tasks, executorProp.getExecutor(), wait, FlowTests.ASSERT_LAST);
  }

  @Test
  public void testLazy_noComplete() {
    createFlow(LazyFiringStrategy::new);
    final int runs = 10;
    final List<Integer> dispatched = new CopyOnWriteArrayList<>();

    for (int i = 0; i < runs; i++) {
      flow.begin(i, new TestTask(dispatched, i));
      flow.begin(i, new TestTask(dispatched, i));
    }
    
    for (int i = 0; i < runs; i++) {
      flow.getPendingConfirmations().get(i).confirm();
    }

    Threads.sleep(10);
    assertEquals(0, dispatched.size());
    assertEquals(runs, flow.getPendingConfirmations().size());
  }

  @Test
  public void testLazy_increasing() {
    createFlow(LazyFiringStrategy::new);
    final int runs = 100;
    final List<Integer> expected = increasingListOf(runs);
    final List<Integer> dispatched = new CopyOnWriteArrayList<>();
    final List<StatefulConfirmation> cons = new ArrayList<>(runs);

    Threads.sleep(10); // covers the idle wait branch of StrictFiringStrategy.cycle()
    expected.forEach(i -> cons.add(flow.begin(i, new TestTask(dispatched, i))));
    ListQuery.of(cons).delayedBy(1).forEach(a -> a.confirm());

    wait.until(ListQuery.of(dispatched).contains(runs - 1));
    assertThat(ListQuery.of(dispatched).isOrderedBy(Integer::compare));
    assertEquals(0, flow.getPendingConfirmations().size());
  }

  @Test
  public void testLazy_decreasing() {
    createFlow(LazyFiringStrategy::new);
    final int runs = 100;
    final List<Integer> expected = increasingListOf(runs);
    final List<Integer> dispatched = new CopyOnWriteArrayList<>();
    final List<StatefulConfirmation> cons = new ArrayList<>(runs);

    expected.forEach(i -> cons.add(flow.begin(i, new TestTask(dispatched, i))));
    ListQuery.of(cons).transform(Collections::reverse).list().forEach(a -> a.confirm());

    wait.until(ListQuery.of(dispatched).contains(runs - 1));
    assertEquals(1, dispatched.size());
    assertEquals(0, flow.getPendingConfirmations().size());
  }

  @Test
  public void testLazy_random() {
    createFlow(LazyFiringStrategy::new);
    final int runs = 100;
    final List<Integer> expected = increasingListOf(runs);
    final List<Integer> dispatched = new CopyOnWriteArrayList<>();
    final List<StatefulConfirmation> cons = new ArrayList<>(runs);

    expected.forEach(i -> cons.add(flow.begin(i, new TestTask(dispatched, i))));
    ListQuery.of(cons).transform(Collections::shuffle).delayedBy(1).forEach(a -> a.confirm());

    wait.until(ListQuery.of(dispatched).contains(runs - 1));
    assertThat(ListQuery.of(dispatched).isOrderedBy(Integer::compare));
    assertEquals(0, flow.getPendingConfirmations().size());
  }
}
