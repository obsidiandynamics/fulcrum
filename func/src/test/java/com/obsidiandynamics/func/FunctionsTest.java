package com.obsidiandynamics.func;

import static java.util.Arrays.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.stream.*;

import org.junit.*;
import org.junit.rules.*;

import com.obsidiandynamics.assertion.*;
import com.obsidiandynamics.func.Functions.*;
import com.obsidiandynamics.junit.*;

public final class FunctionsTest {
  @ClassRule
  public static final ExecutorProp executorProp = new ExecutorProp(Executors::newWorkStealingPool);
  
  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(Functions.class);
  }
  
  @Test
  public void testChainSupplierToFunction() {
    final CheckedSupplier<String, RuntimeException> chained = Functions.chain(() -> "world", "hello_"::concat);
    assertEquals("hello_world", chained.get());
  }
  
  @Test
  public void testChainFunctionToFunction() {
    final CheckedFunction<String, String, RuntimeException> chained = Functions.chain(Functions.identity(), "hello_"::concat);
    assertEquals("hello_world", chained.apply("world"));
  }
  
  @Test
  public void testChainFunctionToConsumer() {
    final AtomicReference<String> ref = new AtomicReference<>();
    final CheckedConsumer<String, RuntimeException> chained = Functions.chain("hello_"::concat, ref::set);
    chained.accept("world");
    assertEquals("hello_world", ref.get());
  }

  @Test
  public void testMapValuesWithNonNull() {
    final Map<String, Integer> numbers = MapBuilder.init("zero", 0).with("one", 1).with("two", 2).build();
    final LinkedHashMap<String, Object> doubled = Functions.mapValues(numbers, n -> n * 2);
    assertEquals(MapBuilder.init("zero", 0).with("one", 2).with("two", 4).build(), doubled);
  }

  @Test
  public void testMapCollectionWithNonNull() {
    final List<Integer> numbers = Arrays.asList(0, 1, 2, 3);
    final ArrayList<Object> doubled = Functions.mapCollection(numbers, n -> n * 2);
    assertEquals(Arrays.asList(0, 2, 4, 6), doubled);
  }

  @Test
  public void testMapValuesWithNull() {
    assertNull(Functions.<String, Integer, Integer, RuntimeException>mapValues(null, n -> n * 2));
  }

  @Test
  public void testMapCollectionWithNull() {
    assertNull(Functions.<Integer, Integer, RuntimeException>mapCollection(null, n -> n * 2));
  }

  @Test
  public void testMapMustExistWithNotNull() throws Exception {
    final Map<String, String> map = new HashMap<>();
    map.put("KEY", "VALUE");
    final String value = Functions.mustExist(map, "KEY", "No value for key %s", Exception::new);
    assertEquals("VALUE", value);
  }

  @Test
  public void testMapMustExistWithNull() throws Exception {
    expectedException.expect(Exception.class);
    expectedException.expectMessage("No value for key KEY");
    Functions.mustExist(Collections.emptyMap(), "KEY", "No value for key %s", Exception::new);
  }

  @Test
  public void testMustBeSubtypeWithCorrectType() throws Exception {
    final String string = Functions.mustBeSubtype("string", String.class, Exception::new);
    assertEquals(string, "string");
  }

  @Test(expected=Exception.class)
  public void testMustBeSubtypeWithIncorrectType() throws Exception {
    Functions.mustBeSubtype("string", Integer.class, Exception::new);
  }
  
  @Test
  public void testMustExistWithNotNull() throws Exception {
    assertNotNull(Functions.mustExist("foo", Exception::new));
  }

  @Test(expected=Exception.class)
  public void testMustExistWithNull() throws Exception {
    Functions.mustExist(null, Exception::new);
  }

  @Test
  public void testMustBeNullWithNullValue() throws Exception {
    Functions.mustBeNull(null, Exception::new);
  }

  @Test(expected=Exception.class)
  public void testMustBeNullWithNonNullValue() throws Exception {
    Functions.mustBeNull("nonNull", Exception::new);
  }

  @Test
  public void testMustBeEqualWithEqualValues() throws Exception {
    Functions.mustBeEqual(42, 42, Exception::new);
  }

  @Test(expected=Exception.class)
  public void testMustBeEqualWithNonEqualValues() throws Exception {
    Functions.mustBeEqual(42, 17, Exception::new);
  }

  @Test
  public void testMustNotBeEqualWithNonEqualValues() throws Exception {
    Functions.mustNotBeEqual(43, 42, Exception::new);
  }

  @Test(expected=Exception.class)
  public void testMustNotBeEqualWithEqualValues() throws Exception {
    Functions.mustNotBeEqual(42, 42, Exception::new);
  }

  @Test
  public void testWithMessageString() {
    final Supplier<Throwable> exceptionSupplier = Functions.withMessage("message", Exception::new);
    final Throwable exception = exceptionSupplier.get();
    assertNotNull(exception);
    assertEquals(Exception.class, exception.getClass());
    assertEquals("message", exception.getMessage());
  }

  @Test
  public void testWithMessageSupplier() {
    final Supplier<Throwable> exceptionSupplier = Functions.withMessage(() -> "supplied message", Exception::new);
    final Throwable exception = exceptionSupplier.get();
    assertNotNull(exception);
    assertEquals(Exception.class, exception.getClass());
    assertEquals("supplied message", exception.getMessage());
  }

  @Test
  public void testIfPresent() {
    assertEquals("with_existing", Functions.ifPresent("existing", "with_"::concat));
    assertNull(Functions.ifPresent((String) null, "with_"::concat));
  }

  @Test
  public void testIfPresentOptional() {
    assertEquals("with_existing", Functions.ifPresentOptional(Optional.of("existing"), "with_"::concat));
    assertNull(Functions.ifPresentOptional(Optional.empty(), "with_"::concat));
  }

  @Test
  public void testIfAbsent() {
    assertEquals("existing", Functions.ifAbsent("existing", () -> "new"));
    assertEquals("new", Functions.ifAbsent((String) null, () -> "new"));
  }

  @Test
  public void testIfAbsentOptional() {
    assertEquals("existing", Functions.ifAbsentOptional(Optional.of("existing"), () -> "new"));
    assertEquals("new", Functions.ifAbsentOptional(Optional.empty(), () -> "new"));
  }

  @Test
  public void testIfEither() {
    assertEquals("with_existing", Functions.ifEither("existing", "with_"::concat, () -> "new"));
    assertEquals("new", Functions.ifEither((String) null, "with_"::concat, () -> "new"));
  }

  @Test
  public void testIfEitherOptional() {
    assertEquals("with_existing", Functions.ifEitherOptional(Optional.of("existing"), "with_"::concat, () -> "new"));
    assertEquals("new", Functions.ifEitherOptional(Optional.empty(), "with_"::concat, () -> "new"));
  }

  @Test
  public void testIfThrew() {
    assertEquals("no throw", Functions.ifThrew(() -> "no throw", () -> "threw"));
    assertEquals("threw", Functions.ifThrew(() -> { throw new RuntimeException(); }, () -> "threw"));
  }

  @Test
  public void testGiveNull() {
    assertNull(Functions.giveNull().get());
  }

  @Test
  public void testGiveValue() {
    assertEquals("test", Functions.give("test").get());
  }
  
  @Test
  public void testIdentity() {
    final Object obj = new Object();
    assertSame(obj, Functions.identity().apply(obj));
  }
  
  @Test
  public void testWithSummary() {
    final Exception cause = new Exception("Simulated");
    final ExceptionHandler exceptionHandler = mock(ExceptionHandler.class);
    final Consumer<Throwable> exceptionConsumer = Functions.withSummary("summary", exceptionHandler);
    exceptionConsumer.accept(cause);
    verify(exceptionHandler).onException(eq("summary"), eq(cause));
  }

  @Test
  public void testTryCatchRunnablePass() {
    final ThrowingRunnable errorProneRunnable = (ThrowingRunnable) () -> {};
    final Consumer<Throwable> onError = Classes.<Consumer<Throwable>>cast(mock(Consumer.class));
    Functions.tryCatch(errorProneRunnable, onError);
    verifyNoMoreInteractions(onError);
  }

  @Test
  public void testTryCatchRunnableFail() {
    final Exception cause = new Exception("Simulated");
    final ThrowingRunnable errorProneRunnable = (ThrowingRunnable) () -> { throw cause; };
    final Consumer<Throwable> onError = Classes.<Consumer<Throwable>>cast(mock(Consumer.class));
    Functions.tryCatch(errorProneRunnable, onError);
    verify(onError).accept(eq(cause));
  }

  @Test
  public void testTryCatchSupplierPass() {
    final ThrowingSupplier<String> errorProneSupplier = (ThrowingSupplier<String>) () -> "value";
    final Supplier<String> defaultValueSupplier = Classes.<Supplier<String>>cast(mock(Supplier.class));
    final Consumer<Throwable> onError = Classes.<Consumer<Throwable>>cast(mock(Consumer.class));
    assertEquals("value", Functions.tryCatch(errorProneSupplier, defaultValueSupplier, onError));
    verifyNoMoreInteractions(defaultValueSupplier);
    verifyNoMoreInteractions(onError);
  }

  @Test
  public void testTryCatchSupplierFail() {
    final Exception cause = new Exception("Simulated");
    final ThrowingSupplier<String> errorProneSupplier = (ThrowingSupplier<String>) () -> { throw cause; };
    final Supplier<String> defaultValueSupplier = (Supplier<String>) () -> "default";
    final Consumer<Throwable> onError = Classes.<Consumer<Throwable>>cast(mock(Consumer.class));
    assertEquals("default", Functions.tryCatch(errorProneSupplier, defaultValueSupplier, onError));
    verify(onError).accept(eq(cause));
  }

  @Test
  public void testIgnoreException() {
    Functions.ignoreException().accept(null);
  }

  @Test
  public void testByFieldSimple() {
    final List<String> strings = asList("dddd", "a", "ccc", "bb");
    Collections.sort(strings, Functions.byField(String::length, Comparator.naturalOrder()));
    assertEquals(asList("a", "bb", "ccc", "dddd"), strings);
    Collections.sort(strings, Functions.byField(String::length, Comparator.naturalOrder()).reversed());
    assertEquals(asList("dddd", "ccc", "bb", "a"), strings);
  }

  @Test
  public void testByFieldComposite() {
    final List<Point> points = asList(new Point(2, 2), new Point(0, 0), new Point(1, 2), new Point(1, 0));
    Collections.sort(points, 
                     new ChainedComparator<Point>()
                     .chain(Functions.byField(Point::getX, Comparator.naturalOrder()))
                     .chain(Functions.byField(Point::getY, Comparator.naturalOrder())));
    assertEquals(asList(new Point(0, 0), new Point(1, 0), new Point(1, 2), new Point(2, 2)), points);
  }

  @Test
  public void testGivePlainNull() {
    assertNull(Functions.givePlainNull().get());
  }

  @Test
  public void testGivePlainValue() {
    assertEquals("test", Functions.givePlain("test").get());
  }

  @Test
  public void testParallelMapStreamWithNonNull() throws InterruptedException {
    final ArrayList<Object> doubled = Functions.parallelMapStream(IntStream.range(0, 4).boxed(), n -> n * 2, executorProp.getExecutor());
    assertEquals(Arrays.asList(0, 2, 4, 6), doubled);
  }

  @Test(expected=RuntimeException.class)
  public void testParallelMapStreamRuntimeException() throws InterruptedException {
    Functions.parallelMapStream(IntStream.range(0, 4).boxed(), n -> {
      throw new RuntimeException("Simulated failure");
    }, executorProp.getExecutor());
  }

  @Test(expected=IOException.class)
  public void testParallelMapStreamCheckedException() throws InterruptedException, IOException {
    Functions.parallelMapStream(IntStream.range(0, 4).boxed(), n -> {
      throw new IOException("Simulated failure");
    }, executorProp.getExecutor());
  }

  @Test
  public void testCapturedExceptionUnwind() {
    final Throwable actualCause = new IOException();
    final Throwable exception = new Exception(new RuntimeException(new CapturedException(actualCause)));
    final Throwable unwound = CapturedException.unwind(exception);
    assertSame(actualCause, unwound);
  }

  @Test(expected=IllegalStateException.class)
  public void testCapturedExceptionUnwindNotCaptured() {
    final Throwable exception = new Exception(new RuntimeException());
    CapturedException.unwind(exception);
  }

  @Test
  public void testVoidFunction() {
    final CheckedConsumer<String, RuntimeException> consumer = Classes.cast(mock(CheckedConsumer.class));
    final CheckedFunction<String, Void, RuntimeException> function = Functions.voidFunction(consumer);
    function.apply("someString");
    verify(consumer).accept(eq("someString"));
  }
}
