package com.obsidiandynamics.flow;

import static com.obsidiandynamics.flow.FlowTests.*;
import static com.obsidiandynamics.flow.FlowTests.assertThat;
import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import com.obsidiandynamics.junit.*;

@RunWith(Parameterized.class)
public final class ThreadlessFlowTest {
  @Parameterized.Parameters
  public static List<Object[]> data() {
    return TestCycle.timesQuietly(1);
  }
  
  private ThreadlessFlow flow;

  private void createFlow(FiringStrategy.Factory firingStrategyFactory) {
    flow = new ThreadlessFlow(firingStrategyFactory);
  }
  
  @Test
  public void testTerminateAndJoin() throws InterruptedException {
    createFlow(StrictFiringStrategy::new);
    assertTrue(flow.terminate().join(0));
  }

  @Test
  public void testStrict_noComplete() {
    createFlow(StrictFiringStrategy::new);
    final int runs = 10;
    final List<Integer> completed = new CopyOnWriteArrayList<>();

    for (int i = 0; i < runs; i++) {
      flow.begin(i, new TestTask(completed, i));
      flow.begin(i, new TestTask(completed, i));
    }

    assertEquals(0, completed.size());
    assertEquals(runs, flow.getPendingConfirmations().size());
  }

  @Test
  public void testStrict_increasing() {
    createFlow(StrictFiringStrategy::new);
    final int runs = 100;
    final List<Integer> expected = increasingListOf(runs);
    final List<Integer> completed = new CopyOnWriteArrayList<>();
    final List<StatefulConfirmation> cons = new ArrayList<>(runs);

    expected.forEach(i -> cons.add(flow.begin(i, new TestTask(completed, i))));
    
    cons.forEach(a -> a.confirm());
    assertThat(ListQuery.of(completed).isSize(runs));
    assertEquals(expected, completed);
    assertEquals(0, flow.getPendingConfirmations().size());
  }

  @Test
  public void testStrict_increasingDoublePending() {
    createFlow(StrictFiringStrategy::new);
    final int runs = 100;
    final List<Integer> expected = increasingListOf(runs);
    final List<Integer> completed = new CopyOnWriteArrayList<>();
    final List<StatefulConfirmation> cons = new ArrayList<>(runs);

    expected.forEach(i -> {
      final StatefulConfirmation c0 = flow.begin(i, new TestTask(completed, i));
      cons.add(c0);
      assertEquals(1, c0.getPendingCount());
      final StatefulConfirmation c1 = flow.begin(i, new TestTask(completed, i));
      assertSame(c0, c1);
      assertEquals(2, c0.getPendingCount());
    });
    
    cons.forEach(a -> a.confirm());
    
    // single pass shouldn't lead to any completions
    assertThat(ListQuery.of(completed).isSize(0));
    
    cons.forEach(a -> a.confirm());

    assertThat(ListQuery.of(completed).isSize(runs));
    assertEquals(expected, completed);
    assertEquals(0, flow.getPendingConfirmations().size());
  }

  @Test
  public void testStrict_decreasing() {
    createFlow(StrictFiringStrategy::new);
    final int runs = 100;
    final List<Integer> expected = increasingListOf(runs);
    final List<Integer> completed = new CopyOnWriteArrayList<>();
    final List<StatefulConfirmation> cons = new ArrayList<>(runs);

    expected.forEach(i -> cons.add(flow.begin(i, new TestTask(completed, i))));
    ListQuery.of(cons).transform(Collections::reverse).list().forEach(a -> a.confirm());

    assertThat(ListQuery.of(completed).isSize(runs));
    assertEquals(expected, completed);
    assertEquals(0, flow.getPendingConfirmations().size());
  }

  @Test
  public void testStrict_random() {
    createFlow(StrictFiringStrategy::new);
    final int runs = 100;
    final List<Integer> expected = increasingListOf(runs);
    final List<Integer> completed = new CopyOnWriteArrayList<>();
    final List<StatefulConfirmation> cons = new ArrayList<>(runs);

    expected.forEach(i -> cons.add(flow.begin(i, new TestTask(completed, i))));
    ListQuery.of(cons).transform(Collections::shuffle).list().forEach(a -> a.confirm());

    assertThat(ListQuery.of(completed).isSize(runs));
    assertEquals(expected, completed);
    assertEquals(0, flow.getPendingConfirmations().size());
  }

  @Test
  public void testLazy_noComplete() {
    createFlow(LazyFiringStrategy::new);
    final int runs = 10;
    final List<Integer> completed = new CopyOnWriteArrayList<>();

    for (int i = 0; i < runs; i++) {
      flow.begin(i, new TestTask(completed, i));
    }

    assertEquals(0, completed.size());
    assertEquals(runs, flow.getPendingConfirmations().size());
  }

  @Test
  public void testLazy_increasing() {
    createFlow(LazyFiringStrategy::new);
    final int runs = 100;
    final List<Integer> expected = increasingListOf(runs);
    final List<Integer> completed = new CopyOnWriteArrayList<>();
    final List<StatefulConfirmation> cons = new ArrayList<>(runs);

    expected.forEach(i -> cons.add(flow.begin(i, new TestTask(completed, i))));
    ListQuery.of(cons).delayedBy(1).forEach(a -> a.confirm());

    assertThat(ListQuery.of(completed).contains(runs - 1));
    assertThat(ListQuery.of(completed).isOrderedBy(Integer::compare));
    assertEquals(0, flow.getPendingConfirmations().size());
  }

  @Test
  public void testLazy_decreasing() {
    createFlow(LazyFiringStrategy::new);
    final int runs = 100;
    final List<Integer> expected = increasingListOf(runs);
    final List<Integer> completed = new CopyOnWriteArrayList<>();
    final List<StatefulConfirmation> cons = new ArrayList<>(runs);

    expected.forEach(i -> cons.add(flow.begin(i, new TestTask(completed, i))));
    ListQuery.of(cons).transform(Collections::reverse).list().forEach(a -> a.confirm());

    assertThat(ListQuery.of(completed).contains(runs - 1));
    assertEquals(1, completed.size());
    assertEquals(0, flow.getPendingConfirmations().size());
  }

  @Test
  public void testLazy_random() {
    createFlow(LazyFiringStrategy::new);
    final int runs = 100;
    final List<Integer> expected = increasingListOf(runs);
    final List<Integer> completed = new CopyOnWriteArrayList<>();
    final List<StatefulConfirmation> cons = new ArrayList<>(runs);

    expected.forEach(i -> cons.add(flow.begin(i, new TestTask(completed, i))));
    ListQuery.of(cons).transform(Collections::shuffle).delayedBy(1).forEach(a -> a.confirm());

    assertThat(ListQuery.of(completed).contains(runs - 1));
    assertThat(ListQuery.of(completed).isOrderedBy(Integer::compare));
    assertEquals(0, flow.getPendingConfirmations().size());
  }
}
