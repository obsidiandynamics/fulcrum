package com.obsidiandynamics.junit;

import java.io.*;
import java.util.function.*;

import org.hamcrest.*;
import org.hamcrest.core.*;

/**
 *  Additional Hamcrest matchers.
 */
public final class HamcrestMatchers {
  private HamcrestMatchers() {}
  
  @SuppressWarnings("unchecked")
  private static <T> T cast(Object obj) {
    return (T) obj;
  }
  
  public static final class TypedMatcherBuilder<T> {
    private final Class<? extends T> expectedType;
    
    TypedMatcherBuilder(Class<? extends T> expectedType) {
      this.expectedType = expectedType;
    }
    
    /**
     *  Matches using the supplied {@code typedMatcher}, predicated on the outcome
     *  of the prior {@link IsInstanceOf} matcher. <p>
     *  
     *  The created matcher assumes no relationship between specified type and the
     *  examined object.
     *  
     *  @param typedMatcher The second-stage matcher to apply.
     *  @return The composite {@link Matcher}.
     */
    public Matcher<Object> thatMatches(Matcher<T> typedMatcher) {
      return HamcrestMatchers.cast(thatMatchesStrict(typedMatcher));
    }
    
    /**
     *  Matches using the supplied {@code typedMatcher}, predicated on the outcome
     *  of the prior {@link IsInstanceOf} matcher. <p>
     *  
     *  The created matcher forces a relationship between specified type and the 
     *  examined object, and should be used when it is necessary to make generics conform.
     *  For example, Mockito's {@code MockitoHamcrest.argThat(Matcher)} requires this.
     *  
     *  @param typedMatcher The second-stage matcher to apply.
     *  @return The composite {@link Matcher}.
     */
    public Matcher<T> thatMatchesStrict(Matcher<T> typedMatcher) {
      final Matcher<Object> instanceOfMatcher = IsInstanceOf.instanceOf(expectedType);
      return AllOf.allOf(instanceOfMatcher, typedMatcher);
    }
  }
  
  /**
   *  A chained conjunction of an {@link IsInstanceOf} matcher that matches 
   *  subclasses of {@code expectedType} with a subsequent matcher that accepts 
   *  values of the {@code expectedType}.
   *  
   *  @param <T> Matched type.
   *  @param expectedType The expected type to match.
   *  @return A chained {@link TypedMatcherBuilder} for specifying the chained matcher.
   */
  public static <T> TypedMatcherBuilder<T> instanceOf(Class<? extends T> expectedType) {
    return new TypedMatcherBuilder<>(expectedType);
  }
  
  /**
   *  Creates a matcher that fulfils a given {@link Predicate}.
   *  
   *  @param <T> Matched type.
   *  @param predicate The predicate for the matcher to pass.
   *  @return The resulting {@link Matcher}.
   */
  public static <T> Matcher<T> fulfils(Predicate<? super T> predicate) {
    return new BaseMatcher<T>() {
      @Override
      public boolean matches(Object item) {
        final T typedItem = HamcrestMatchers.cast(item);
        return predicate.test(typedItem);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("fulfils predicate [" + predicate + "]");
      }
    };
  }

  /**
   *  Creates a matcher that fulfils a given {@link Predicate}. <p>
   *  
   *  This variant will log the thrown {@link AssertionError} to
   *  {@link System#err}.
   *  
   *  @param <T> Matched type.
   *  @param assertion A {@link Consumer} of the tested value that must execute
   *                   without throwing an {@link AssertionError} for the matcher to pass.
   *  @return The created {@link Matcher}.
   */
  public static <T> Matcher<T> assertedBy(Consumer<? super T> assertion) {
    return assertedBy(assertion, forPrintStream(System.err));
  }
  
  /**
   *  Creates an exception handler for the given print stream.
   *  
   *  @param printStream The print stream to pipe the exception's stack trace to.
   *  @return The exception handler.
   */
  public static Consumer<AssertionError> forPrintStream(PrintStream printStream) {
    return e -> e.printStackTrace(printStream);
  }
  
  /**
   *  Creates a matcher that requires the given {@code assertion} block to pass. <p>
   *  
   *  In addition to failing the matcher, the given {@code exceptionHandler} will
   *  also be invoked with the thrown {@link AssertionError}.
   *  
   *  @param <T> Matched type.
   *  @param assertion A {@link Consumer} of the tested value that must execute
   *                   without throwing an {@link AssertionError} for the matcher to pass.
   *  @param exceptionHandler Invoked if the assertion fails.
   *  @return The created {@link Matcher}.
   */
  public static <T> Matcher<T> assertedBy(Consumer<? super T> assertion, Consumer<? super AssertionError> exceptionHandler) {
    return new BaseMatcher<T>() {
      @Override
      public boolean matches(Object item) {
        final T typedItem = HamcrestMatchers.cast(item);
        try {
          assertion.accept(typedItem);
          return true;
        } catch (AssertionError e) {
          exceptionHandler.accept(e);
          return false;
        }
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("asserted by [" + assertion + "]");
      }
    };
  }
}
