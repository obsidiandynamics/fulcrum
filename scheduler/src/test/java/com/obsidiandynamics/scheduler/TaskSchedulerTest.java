package com.obsidiandynamics.scheduler;

import static junit.framework.TestCase.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import com.obsidiandynamics.await.*;
import com.obsidiandynamics.junit.*;
import com.obsidiandynamics.testmark.*;
import com.obsidiandynamics.threads.*;

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class TaskSchedulerTest {
  @Parameterized.Parameters
  public static List<Object[]> data() {
    return TestCycle.timesQuietly(1);
  }
  
  private static final class TestTask extends AbstractTask<UUID> {
    private final Consumer<TestTask> taskBody;
    
    TestTask(long time, UUID id, Consumer<TestTask> taskBody) {
      super(time, id);
      this.taskBody = taskBody;
    }
    
    @Override
    public void execute(TaskScheduler scheduler) {
      taskBody.accept(this);
    }
  }
  
  private static final class Receiver {
    private final List<UUID> ids = new CopyOnWriteArrayList<>();
    
    void receive(TestTask task) {
      ids.add(task.getId());
    }
    
    Runnable isSize(int size) {
      return () -> assertEquals(size, ids.size());
    }
  }
  
  private Receiver receiver;
  
  private TaskScheduler scheduler;
  
  private final Timesert wait = Timesert.wait(10_000);
  
  @Before
  public void setup() {
    receiver = new Receiver();
    resetTaskScheduler(new TaskScheduler());
  }
  
  @After
  public void teardown() throws InterruptedException {
    scheduler.terminate().joinSilently();
    scheduler.join(); // should be a no-op
  }
  
  void resetTaskScheduler(TaskScheduler scheduler) {
    if (this.scheduler != null) {
      this.scheduler.terminate().joinSilently();
    }
    this.scheduler = scheduler;
    scheduler.start();
  }
  
  @Test
  public void testSchedule() {
    testSchedule(10);
  }
  
  private void testSchedule(int tasks) {
    final List<UUID> ids = new ArrayList<>(tasks);
    for (int i = 0; i < tasks; i++) {
      final TestTask task = doIn(new UUID(0, i), i);
      ids.add(task.getId());
      scheduler.schedule(task);
    }
   
    wait.until(receiver.isSize(tasks));
    assertEquals(ids, receiver.ids);
  }
  
  @Test
  public void testScheduleReverse() {
    testScheduleReverse(10);
  }
  
  private void testScheduleReverse(int tasks) {
    // park the scheduler until we're ready to execute
    final CyclicBarrier barrier = new CyclicBarrier(2);
    scheduler.schedule(new AbstractTask<Long>(0, -1L) {
      @Override public void execute(TaskScheduler scheduler) {
        Threads.await(barrier);
      }
    });
    
    final List<UUID> ids = new ArrayList<>(tasks);  
    final long referenceNanos = System.nanoTime();
    for (int i = tasks; --i >= 0; ) {
      final TestTask task = doIn(new UUID(0, i), referenceNanos, i);
      ids.add(task.getId());
      
      // alternate method call order to cover the double-checked adjustment of wake horizon
      if (i % 2 == 0) {
        scheduler.schedule(task);
        scheduler.adjustWakeHorizon(task); 
      } else {
        scheduler.adjustWakeHorizon(task); 
        scheduler.schedule(task);
      }
    }
    
    Threads.await(barrier); // resume scheduling
   
    Collections.reverse(ids);
    wait.until(receiver.isSize(tasks));
    assertEquals(ids, receiver.ids);
  }
  
  @Test
  public void testParallelSchedule() {
    testParallelSchedule(8, 10);
  }
  
  private void testParallelSchedule(int addThreads, int tasksPerThread) {
    Parallel.blocking(addThreads, threadIdx -> {
      final List<UUID> ids = new ArrayList<>(tasksPerThread);
      for (int i = 0; i < tasksPerThread; i++) {
        final TestTask task = doIn(new UUID(threadIdx, i), i);
        ids.add(task.getId());
        scheduler.schedule(task);
      }
    }).run();
    
    wait.until(receiver.isSize(addThreads * tasksPerThread));
  }
  
  @Test
  public void testClear() {
    testClear(10);
  }
  
  private void testClear(int tasks) {
    final List<UUID> ids = new ArrayList<>(tasks);
    for (int i = 0; i < tasks; i++) {
      final TestTask task = doIn(new UUID(0, i), i);
      ids.add(task.getId());
      scheduler.schedule(task);
    }
   
    scheduler.clear();
    Threads.sleep(10);
    assertTrue(receiver.ids.size() <= tasks);
  }
  
  @Test
  public void testForceExecute() {
    testForceExecute(10);
  }
  
  private void testForceExecute(int tasks) {
    final List<UUID> ids = new ArrayList<>(tasks); 
    for (int i = 0; i < tasks; i++) {
      final TestTask task = doIn(60_000 + i * 1_000);
      ids.add(task.getId());
      scheduler.schedule(task);
    }
   
    assertEquals(0, receiver.ids.size());
    scheduler.forceExecute();
    wait.until(receiver.isSize(tasks));
    assertEquals(ids, receiver.ids);
  }
  
  @Test
  public void testAbort() {
    testAbort(10);
  }
  
  private void testAbort(int tasks) {
    final List<TestTask> timeouts = new ArrayList<>(tasks); 
    for (int i = 0; i < tasks; i++) {
      final TestTask task = doIn(60_000 + i * 1_000);
      timeouts.add(task);
      scheduler.schedule(task);
    }
    
    assertEquals(0, receiver.ids.size());
    
    for (TestTask task : timeouts) {
      assertTrue(scheduler.abort(task));
      assertFalse(scheduler.abort(task)); // 2nd call should have no effect
    }
    
    assertEquals(0, receiver.ids.size());
    scheduler.forceExecute();
    assertEquals(0, receiver.ids.size());
  }
  
  @Test
  public void testAbortBeforeExecute() {
    final String threadName = TaskSchedulerTest.class.getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(this));
    resetTaskScheduler(new TaskScheduler(threadName));
    
    final int runs = 10;
    final AtomicInteger executed = new AtomicInteger();
    for (int i = 0; i < runs; i++) {
      final long time = System.nanoTime() + i * 1_000_000L;
      final Task task = new Task() {
        private boolean aborting;
        
        @Override
        public long getTime() {
          if (Thread.currentThread().getName().equals(threadName) && ! aborting) {
            aborting = true;
            scheduler.abort(this);
          }
          return time;
        }
  
        @Override
        public Comparable<?> getId() {
          return 0;
        }
  
        @Override
        public void execute(TaskScheduler scheduler) {
          executed.incrementAndGet();
        }
      };
      scheduler.schedule(task);
    }
    
    scheduler.terminate().joinSilently();
    assertEquals(0, executed.get());
  }

  @Test
  public void testEarlyExecute() {
    testEarlyExecute(10);
  }
  
  private void testEarlyExecute(int tasks) {
    final List<TestTask> timeouts = new ArrayList<>(tasks); 
    for (int i = 0; i < tasks; i++) {
      final TestTask task = doIn(new UUID(0, i), 60_000 + i * 1_000);
      timeouts.add(task);
      scheduler.schedule(task);
    }
   
    assertEquals(0, receiver.ids.size());
    for (TestTask task : timeouts) {
      scheduler.executeNow(task);
      scheduler.executeNow(task); // 2nd call should have no effect
    }
    assertEquals(tasks, receiver.ids.size());
    scheduler.forceExecute();
    assertEquals(tasks, receiver.ids.size());
    
    final List<UUID> sorted = new ArrayList<>(receiver.ids);
    Collections.sort(sorted);
    assertEquals(sorted, receiver.ids);
  }
  
  @Test
  public void testScheduleSingleNoTasks() {
    final NavigableSet<Task> tasks = new ConcurrentSkipListSet<>(TaskScheduler::compareByTimeAndId);
    TaskScheduler.scheduleSingle(tasks, null, true);
  }
  
  private static class MockTask implements Task {
    private final long time;
    
    private final int id;
    
    MockTask(long time, int id) {
      this.time = time;
      this.id = id;
    }
    
    @Override
    public void execute(TaskScheduler scheduler) {}

    @Override
    public Comparable<?> getId() {
      return id;
    }

    @Override
    public long getTime() {
      return time;
    }
  }
  
  @Test
  public void testScheduleSingleExecuteUnforced() {
    final NavigableSet<Task> tasks = new ConcurrentSkipListSet<>(TaskScheduler::compareByTimeAndId);
    final MockTask task = new MockTask(0, 1);
    final MockTask spied = spy(task);
    tasks.add(spied);
    TaskScheduler.scheduleSingle(tasks, null, false);
    verify(spied).execute(any());
  }
  
  @Test
  public void testScheduleSingleExecuteForced() {
    final NavigableSet<Task> tasks = new ConcurrentSkipListSet<>(TaskScheduler::compareByTimeAndId);
    final MockTask task = new MockTask(Long.MAX_VALUE, 1);
    final MockTask spied = spy(task);
    tasks.add(spied);
    TaskScheduler.scheduleSingle(tasks, null, true);
    verify(spied).execute(any());
  }
  
  @Test
  public void testScheduleSingleNotTime() {
    final NavigableSet<Task> tasks = new ConcurrentSkipListSet<>(TaskScheduler::compareByTimeAndId);
    final MockTask task = new MockTask(Long.MAX_VALUE, 1);
    final MockTask spied = spy(task);
    tasks.add(spied);
    TaskScheduler.scheduleSingle(tasks, null, false);
    verify(spied, never()).execute(any());
  }
  
  @Test
  public void testScheduleSingleNoRemove() {
    final NavigableSet<Task> tasks = new ConcurrentSkipListSet<>(TaskScheduler::compareByTimeAndId);
    final MockTask task = new MockTask(0, 1);
    final MockTask spied = spy(task);
    tasks.add(spied);
    when(spied.getTime()).then((invocation) -> {
      tasks.clear();
      return 0L;
    });
    TaskScheduler.scheduleSingle(tasks, null, false);
    verify(spied, never()).execute(any());
  }
  
  private TestTask doIn(long millis) {
    return doIn(UUID.randomUUID(), millis);
  }
  
  private TestTask doIn(UUID uuid, long millis) {
    return doIn(uuid, System.nanoTime(), millis);
  }
  
  private TestTask doIn(UUID uuid, long referenceNanos, long millis) {
    return new TestTask(referenceNanos + millis * 1_000_000l, 
                        uuid,
                        receiver::receive);
  }

  @Test
  public void testScheduleVolume() {
    testScheduleVolumeBenchmark(10_000,
                                1,
                                10_000_000,
                                50_000_000,
                                false);
  }
  
  @Test
  public void testScheduleVolumeBenchmark() {
    Testmark.ifEnabled(() -> {
      final int scale = Testmark.getOptions(Scale.class, Scale.unity()).magnitude();
      testScheduleVolumeBenchmark(10_000_000L * scale,
                                  1,
                                  10_000_000,
                                  50_000_000,
                                  true);
    });
  }
  
  private void testScheduleVolumeBenchmark(long tasks, int submissionThreads, long minDelayNanos, 
                                           long maxDelayNanos, boolean logResults) {
    final AtomicLong fired = new AtomicLong();
    final Consumer<TestTask> counter = tt -> fired.incrementAndGet();
    final long startNanos = System.nanoTime();

    final long tasksPerThread = tasks / submissionThreads;
    Parallel.blocking(submissionThreads, threadIdx -> {
      for (int i = 0; i < tasksPerThread; i++) {
        final long delayNanos = (long) (Math.random() * (maxDelayNanos - minDelayNanos)) + minDelayNanos;
        final TestTask task = new TestTask(startNanos + delayNanos, 
                                           new UUID(threadIdx, i),
                                           counter);
        scheduler.schedule(task);
      }
    }).run();
   
    final long expectedTasks = tasksPerThread * submissionThreads;
    wait.until(() -> assertEquals(expectedTasks, fired.get()));
    final long took = System.nanoTime() - startNanos - minDelayNanos;

    if (logResults) {
      System.out.format("Schedule volume: %,d took %,d ms, %,.0f tasks/sec\n", 
                        expectedTasks, took / 1_000_000L, (double) expectedTasks / took * 1_000_000_000L);
    }
  }
  
  public static void main(String[] args) {
    Testmark.enable();
    JUnitCore.runClasses(TaskSchedulerTest.class);
  }
}
