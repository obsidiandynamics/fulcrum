package com.obsidiandynamics.mockito;


import static com.obsidiandynamics.func.Functions.*;

import java.util.*;
import java.util.concurrent.*;

import org.mockito.invocation.*;
import org.mockito.stubbing.*;

import com.obsidiandynamics.func.*;

/**
 *  Captures return values of mocked methods.
 *  
 *  @param <T> Answer type.
 */
public final class ResultCaptor<T> implements Answer<T> {
  public static final class Capture<T> {
    private static final Capture<?> empty = new Capture<>(null, null);
    
    private final T result;
    private final Throwable exception;
    
    private Capture(T result, Throwable throwable) { 
      this.result = result; 
      this.exception = throwable;
    }
    
    public static <T> Capture<T> result(T result) { 
      return result != null ? new Capture<>(result, null) : empty(); 
    }
    
    public static <T> Capture<T> exception(Throwable exception) { 
      return new Capture<>(null, exception); 
    }
    
    public static <T> Capture<T> empty() {
      return Classes.cast(empty);
    }
    
    @Override
    public int hashCode() {
      return Objects.hash(result, exception);
    }
    
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if (obj instanceof Capture) {
        final Capture<?> that = (Capture<?>) obj;
        return Objects.equals(result, that.result) && Objects.equals(exception, that.exception);
      } else {
        return false;
      }
    }
    
    @Override
    public String toString() {
      return Capture.class.getSimpleName() + " [result=" + result + ", exception=" + exception + "]";
    }

    public T result() { return result; }
    
    public Throwable exception() { return exception; }
    
    public boolean isException() {
      return exception != null;
    }
  }
  
  /** The delegate mock that will provide the actual answer. */
  private final Answer<? extends T> answer;
  
  /** Maps arguments to return values. */
  private final Map<List<Object>, Capture<T>> results = new ConcurrentHashMap<>();
  
  private ResultCaptor(Answer<? extends T> answer) {
    this.answer = answer;
  }
  
  /**
   *  Wraps a delegate {@code answer} mock in a {@link ResultCaptor} instance.
   *  
   *  @param <T> The captured result type.
   *  @param answer The delegate mock that will provide the actual answer.
   *  @return The {@link ResultCaptor}.
   */
  public static <T> ResultCaptor<T> of(Answer<? extends T> answer) {
    return new ResultCaptor<>(answer);
  }
  
  static final class NoResultForArgsError extends AssertionError {
    private static final long serialVersionUID = 1L;
    
    NoResultForArgsError(String m) { super(m); }
  }
  
  public Capture<T> get(Object... args) {
    return get(Arrays.asList(args));
  }
  
  public Capture<T> get(List<Object> args) {
    return mustExist(results, args, "No capture for args %s", NoResultForArgsError::new);
  }
  
  public int count() {
    return results.size();
  }
  
  public Map<List<Object>, Capture<T>> all() {
    return Collections.unmodifiableMap(results);
  }

  @Override
  public T answer(InvocationOnMock invocation) throws Throwable {
    final List<Object> args = Arrays.asList(invocation.getArguments());
    try {
      final T result = answer.answer(invocation);
      results.put(args, Capture.result(result));
      return result;
    } catch (Throwable e) {
      results.put(args, Capture.exception(e));
      throw e;
    }
  }
}
