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

import org.hamcrest.core.*;
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
  public void testChain_supplierToFunction() {
    final CheckedSupplier<String, RuntimeException> chained = Functions.chain(() -> "world", "hello_"::concat);
    assertEquals("hello_world", chained.get());
  }
  
  @Test
  public void testChain_functionToFunction() {
    final CheckedFunction<String, String, RuntimeException> chained = Functions.chain(CheckedFunction.identity(), "hello_"::concat);
    assertEquals("hello_world", chained.apply("world"));
  }
  
  @Test
  public void testChain_functionToConsumer() {
    final AtomicReference<String> ref = new AtomicReference<>();
    final CheckedConsumer<String, RuntimeException> chained = Functions.chain("hello_"::concat, ref::set);
    chained.accept("world");
    assertEquals("hello_world", ref.get());
  }

  @Test
  public void testMapValues_withNonNull() {
    final Map<String, Integer> numbers = MapBuilder.init("zero", 0).with("one", 1).with("two", 2).build();
    final LinkedHashMap<String, Object> doubled = Functions.mapValues(numbers, n -> n * 2);
    assertEquals(MapBuilder.init("zero", 0).with("one", 2).with("two", 4).build(), doubled);
  }

  @Test
  public void testMapCollection_withNonNull() {
    final List<Integer> numbers = Arrays.asList(0, 1, 2, 3);
    final ArrayList<Object> doubled = Functions.mapCollection(numbers, n -> n * 2);
    assertEquals(Arrays.asList(0, 2, 4, 6), doubled);
  }

  @Test
  public void testMapValues_withNull() {
    assertNull(Functions.<String, Integer, Integer, RuntimeException>mapValues(null, n -> n * 2));
  }

  @Test
  public void testMapCollection_withNull() {
    assertNull(Functions.<Integer, Integer, RuntimeException>mapCollection(null, n -> n * 2));
  }

  @Test
  public void testMustExist_mapWithNotNull() throws Exception {
    final Map<String, String> map = new HashMap<>();
    map.put("KEY", "VALUE");
    final String value = Functions.mustExist(map, "KEY", "No value for key %s", Exception::new);
    assertEquals("VALUE", value);
  }

  @Test
  public void testMustExist_mapWithNull() throws Exception {
    expectedException.expect(Exception.class);
    expectedException.expectMessage("No value for key KEY");
    Functions.mustExist(Collections.emptyMap(), "KEY", "No value for key %s", Exception::new);
  }

  @Test
  public void testMustBeSubtype_withCorrectType() throws Exception {
    final String string = Functions.mustBeSubtype("string", String.class, Exception::new);
    assertEquals(string, "string");
  }

  @Test(expected=Exception.class)
  public void testMustBeSubtype_withIncorrectType() throws Exception {
    Functions.mustBeSubtype("string", Integer.class, Exception::new);
  }

  @Test(expected=Exception.class)
  public void testMustBeSubtype_withNull() throws Exception {
    Functions.mustBeSubtype(null, String.class, Exception::new);
  }
  
  @Test
  public void testMustExist_withNotNull() throws Exception {
    assertEquals("foo", Functions.mustExist("foo", Exception::new));
  }

  @Test(expected=Exception.class)
  public void testMustExist_withNull() throws Exception {
    Functions.mustExist(null, Exception::new);
  }

  @Test
  public void testMustExist_withNotNullUsingNullArgumentExceptionWithMessageVariant() throws Exception {
    assertEquals("foo", Functions.mustExist("foo", "Something cannot be null"));
  }

  @Test
  public void testMustExist_withNullUsingNullArgumentExceptionWithMessageVariant() throws Exception {
    expectedException.expect(NullArgumentException.class);
    expectedException.expectMessage("Something cannot be null");
    Functions.mustExist(null, "Something cannot be null");
  }

  @Test
  public void testMustExist_withNotNullUsingNullArgumentExceptionWithoutMessageVariant() throws Exception {
    assertEquals("foo", Functions.mustExist("foo"));
  }

  @Test
  public void testMustExist_withNullUsingNullArgumentExceptionWithoutMessageVariant() throws Exception {
    expectedException.expect(NullArgumentException.class);
    expectedException.expectMessage(IsNull.nullValue(String.class));
    Functions.mustExist(null);
  }

  @Test
  public void testMustBeNull_withNullValue() throws Exception {
    Functions.mustBeNull(null, Exception::new);
  }

  @Test(expected=Exception.class)
  public void testMustBeNull_withNonNullValue() throws Exception {
    Functions.mustBeNull("nonNull", Exception::new);
  }

  @Test
  public void testMustBeEqual_withEqualValues() throws Exception {
    Functions.mustBeEqual(42, 42, Exception::new);
  }

  @Test(expected=Exception.class)
  public void testMustBeEqual_withNonEqualValues() throws Exception {
    Functions.mustBeEqual(42, 17, Exception::new);
  }

  @Test
  public void testMustNotBeEqual_withNonEqualValues() throws Exception {
    assertEquals(42, Functions.mustNotBeEqual(43, 42, Exception::new).intValue());
  }

  @Test(expected=Exception.class)
  public void testMustNotBeEqual_withEqualValues() throws Exception {
    Functions.mustNotBeEqual(42, 42, Exception::new);
  }
  
  @Test
  public void testMustBeGreater_withLong() throws Exception {
    Functions.mustBeGreater(4L, 3L, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeGreater_withLongFail() throws Exception {
    Functions.mustBeGreater(3L, 3L, Exception::new);
  }
  
  @Test
  public void testMustBeGreaterOrEqual_withLong() throws Exception {
    Functions.mustBeGreaterOrEqual(4L, 3L, Exception::new);
    Functions.mustBeGreaterOrEqual(3L, 3L, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeGreaterOrEqual_withLongFail() throws Exception {
    Functions.mustBeGreaterOrEqual(2L, 3L, Exception::new);
  }
  
  @Test
  public void testMustBeLess_withLong() throws Exception {
    Functions.mustBeLess(2L, 3L, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeLess_withLongFail() throws Exception {
    Functions.mustBeLess(3L, 3L, Exception::new);
  }
  
  @Test
  public void testMustBeLessOrEqual_withLong() throws Exception {
    Functions.mustBeLessOrEqual(3L, 3L, Exception::new);
    Functions.mustBeLessOrEqual(2L, 3L, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeLessOrEqual_withLongFail() throws Exception {
    Functions.mustBeLessOrEqual(4L, 3L, Exception::new);
  }
  
  @Test
  public void testMustBeGreater_withInt() throws Exception {
    Functions.mustBeGreater(4, 3, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeGreater_withIntFail() throws Exception {
    Functions.mustBeGreater(3, 3, Exception::new);
  }
  
  @Test
  public void testMustBeGreaterOrEqual_withInt() throws Exception {
    Functions.mustBeGreaterOrEqual(4, 3, Exception::new);
    Functions.mustBeGreaterOrEqual(3, 3, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeGreaterOrEqual_withIntFail() throws Exception {
    Functions.mustBeGreaterOrEqual(2, 3, Exception::new);
  }
  
  @Test
  public void testMustBeLess_withInt() throws Exception {
    Functions.mustBeLess(2, 3, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeLess_withIntFail() throws Exception {
    Functions.mustBeLess(3, 3, Exception::new);
  }
  
  @Test
  public void testMustBeLessOrEqual_withInt() throws Exception {
    Functions.mustBeLessOrEqual(3, 3, Exception::new);
    Functions.mustBeLessOrEqual(2, 3, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeLessOrEqual_withIntFail() throws Exception {
    Functions.mustBeLessOrEqual(4, 3, Exception::new);
  }
  
  @Test
  public void testMustBeGreater_withDouble() throws Exception {
    Functions.mustBeGreater(4.0, 3.0, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeGreater_withDoubleFail() throws Exception {
    Functions.mustBeGreater(3.0, 3.0, Exception::new);
  }
  
  @Test
  public void testMustBeGreaterOrEqual_withDouble() throws Exception {
    Functions.mustBeGreaterOrEqual(4.0, 3.0, Exception::new);
    Functions.mustBeGreaterOrEqual(3.0, 3.0, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeGreaterOrEqual_withDoubleFail() throws Exception {
    Functions.mustBeGreaterOrEqual(2.0, 3.0, Exception::new);
  }
  
  @Test
  public void testMustBeLess_withDouble() throws Exception {
    Functions.mustBeLess(2.0, 3.0, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeLess_withDoubleFail() throws Exception {
    Functions.mustBeLess(3.0, 3.0, Exception::new);
  }
  
  @Test
  public void testMustBeLessOrEqual_withDouble() throws Exception {
    Functions.mustBeLessOrEqual(3.0, 3.0, Exception::new);
    Functions.mustBeLessOrEqual(2.0, 3.0, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeLessOrEqual_withDoubleFail() throws Exception {
    Functions.mustBeLessOrEqual(4.0, 3.0, Exception::new);
  }
  
  @Test
  public void testMustBeGreater_withFloat() throws Exception {
    Functions.mustBeGreater(4.0f, 3.0f, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeGreater_withFloatFail() throws Exception {
    Functions.mustBeGreater(3.0f, 3.0f, Exception::new);
  }
  
  @Test
  public void testMustBeGreaterOrEqual_withFloat() throws Exception {
    Functions.mustBeGreaterOrEqual(4.0f, 3.0f, Exception::new);
    Functions.mustBeGreaterOrEqual(3.0f, 3.0f, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeGreaterOrEqual_withFloatFail() throws Exception {
    Functions.mustBeGreaterOrEqual(2.0f, 3.0f, Exception::new);
  }
  
  @Test
  public void testMustBeLess_withFloat() throws Exception {
    Functions.mustBeLess(2.0f, 3.0f, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeLess_withFloatFail() throws Exception {
    Functions.mustBeLess(3.0f, 3.0f, Exception::new);
  }
  
  @Test
  public void testMustBeLessOrEqual_withFloat() throws Exception {
    Functions.mustBeLessOrEqual(3.0f, 3.0f, Exception::new);
    Functions.mustBeLessOrEqual(2.0f, 3.0f, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeLessOrEqual_withFloatFail() throws Exception {
    Functions.mustBeLessOrEqual(4.0f, 3.0f, Exception::new);
  }
  
  @Test
  public void testMustBeGreater_withComparable() throws Exception {
    Functions.mustBeGreater((Integer) 4, (Integer) 3, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeGreater_withComparableFail() throws Exception {
    Functions.mustBeGreater((Integer) 3, (Integer) 3, Exception::new);
  }
  
  @Test
  public void testMustBeGreaterOrEqual_withComparable() throws Exception {
    Functions.mustBeGreaterOrEqual((Integer) 4, (Integer) 3, Exception::new);
    Functions.mustBeGreaterOrEqual((Integer) 3, (Integer) 3, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeGreaterOrEqual_withComparableFail() throws Exception {
    Functions.mustBeGreaterOrEqual((Integer) 2, (Integer) 3, Exception::new);
  }
  
  @Test
  public void testMustBeLess_withComparable() throws Exception {
    Functions.mustBeLess((Integer) 2, (Integer) 3, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeLess_withComparableFail() throws Exception {
    Functions.mustBeLess((Integer) 3, (Integer) 3, Exception::new);
  }
  
  @Test
  public void testMustBeLessOrEqual_withComparable() throws Exception {
    Functions.mustBeLessOrEqual((Integer) 3, (Integer) 3, Exception::new);
    Functions.mustBeLessOrEqual((Integer) 2, (Integer) 3, Exception::new);
  }
  
  @Test(expected=Exception.class)
  public void testMustBeLessOrEqual_withComparableFail() throws Exception {
    Functions.mustBeLessOrEqual((Integer) 4, (Integer) 3, Exception::new);
  }

  @Test
  public void testIllegalArgument() {
    final Throwable exception = Functions.illegalArgument("message").get();
    assertNotNull(exception);
    assertEquals(IllegalArgumentException.class, exception.getClass());
    assertEquals("message", exception.getMessage());
  }

  @Test
  public void testIllegalState() {
    final Throwable exception = Functions.illegalState("message").get();
    assertNotNull(exception);
    assertEquals(IllegalStateException.class, exception.getClass());
    assertEquals("message", exception.getMessage());
  }

  @Test
  public void testWithMessage_string() {
    final Supplier<Throwable> exceptionSupplier = Functions.withMessage("message", Exception::new);
    final Throwable exception = exceptionSupplier.get();
    assertNotNull(exception);
    assertEquals(Exception.class, exception.getClass());
    assertEquals("message", exception.getMessage());
  }

  @Test
  public void testWithMessage_supplier() {
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
  public void testIfPresentVoid() {
    final CheckedConsumer<String, RuntimeException> consumer = Classes.cast(mock(CheckedConsumer.class));
    
    Functions.ifPresentVoid((String) null, consumer);
    verify(consumer, never()).accept(any());
    
    Functions.ifPresentVoid("test", consumer);
    verify(consumer).accept(eq("test"));
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
  public void testGive() {
    assertEquals("test", Functions.give("test").get());
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
  public void testTryCatch_runnablePass() {
    final ThrowingRunnable errorProneRunnable = (ThrowingRunnable) () -> {};
    final Consumer<Throwable> onError = Classes.<Consumer<Throwable>>cast(mock(Consumer.class));
    Functions.tryCatch(errorProneRunnable, onError);
    verifyNoMoreInteractions(onError);
  }

  @Test
  public void testTryCatch_RunnableFail() {
    final Exception cause = new Exception("Simulated");
    final ThrowingRunnable errorProneRunnable = (ThrowingRunnable) () -> { throw cause; };
    final Consumer<Throwable> onError = Classes.<Consumer<Throwable>>cast(mock(Consumer.class));
    Functions.tryCatch(errorProneRunnable, onError);
    verify(onError).accept(eq(cause));
  }

  @Test
  public void testTryCatch_supplierPass() {
    final ThrowingSupplier<String> errorProneSupplier = (ThrowingSupplier<String>) () -> "value";
    final Supplier<String> defaultValueSupplier = Classes.<Supplier<String>>cast(mock(Supplier.class));
    final Consumer<Throwable> onError = Classes.<Consumer<Throwable>>cast(mock(Consumer.class));
    assertEquals("value", Functions.tryCatch(errorProneSupplier, defaultValueSupplier, onError));
    verifyNoMoreInteractions(defaultValueSupplier);
    verifyNoMoreInteractions(onError);
  }

  @Test
  public void testTryCatch_supplierFail() {
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
  public void testByField_simple() {
    final List<String> strings = asList("dddd", "a", "ccc", "bb");
    Collections.sort(strings, Functions.byField(String::length, Comparator.naturalOrder()));
    assertEquals(asList("a", "bb", "ccc", "dddd"), strings);
    Collections.sort(strings, Functions.byField(String::length, Comparator.naturalOrder()).reversed());
    assertEquals(asList("dddd", "ccc", "bb", "a"), strings);
  }

  @Test
  public void testByField_composite() {
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
  public void testGivePlain() {
    assertEquals("test", Functions.givePlain("test").get());
  }

  @Test
  public void testParallelMapStream_withNonNull() throws InterruptedException {
    final ArrayList<Object> doubled = Functions.parallelMapStream(IntStream.range(0, 4).boxed(), n -> n * 2, executorProp.getExecutor());
    assertEquals(Arrays.asList(0, 2, 4, 6), doubled);
  }

  @Test(expected=RuntimeException.class)
  public void testParallelMapStream_runtimeException() throws InterruptedException {
    Functions.parallelMapStream(IntStream.range(0, 4).boxed(), n -> {
      throw new RuntimeException("Simulated failure");
    }, executorProp.getExecutor());
  }

  @Test(expected=IOException.class)
  public void testParallelMapStream_checkedException() throws InterruptedException, IOException {
    Functions.parallelMapStream(IntStream.range(0, 4).boxed(), n -> {
      throw new IOException("Simulated failure");
    }, executorProp.getExecutor());
  }

  @Test
  public void testCapturedException_unwind() {
    final Throwable actualCause = new IOException();
    final Throwable exception = new Exception(new RuntimeException(new CapturedException(actualCause)));
    final Throwable unwound = CapturedException.unwind(exception);
    assertSame(actualCause, unwound);
  }

  @Test(expected=IllegalStateException.class)
  public void testCapturedException_unwind_notCaptured() {
    final Throwable exception = new Exception(new RuntimeException());
    CapturedException.unwind(exception);
  }
  
  @Test
  public void testForEach() {
    final List<Integer> source = Arrays.asList(1, 2, 3);
    final List<Integer> target = new ArrayList<>(source.size());
    Functions.forEach(source, target::add);
    assertEquals(source, target);
  }

  @Test
  public void testVoidFunction() {
    final CheckedConsumer<String, RuntimeException> consumer = Classes.cast(mock(CheckedConsumer.class));
    final CheckedFunction<String, Void, RuntimeException> function = Functions.voidFunction(consumer);
    function.apply("someString");
    verify(consumer).accept(eq("someString"));
  }
  
  @Test
  public void testFieldPredicate() {
    final List<String> strings = Arrays.asList("hare", "cow", "boar", "ox");
    final List<String> filtered = strings.stream()
        .filter(Functions.fieldPredicate(String::length, length -> length > 3))
        .collect(Collectors.toList());
    org.assertj.core.api.Assertions.assertThat(filtered).containsExactly("hare", "boar");
  }
}
