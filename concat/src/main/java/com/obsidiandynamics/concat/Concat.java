package com.obsidiandynamics.concat;

import java.util.function.*;

/**
 *  A chainable string builder that allows conditional appending of strings, e.g.:<p>
 *  
 *  {@code
 *  new StringChainer("foo").when(someCondition).append(" bar");
 *  }<p>
 *  
 *  Produces "foo" if {@code someCondition} is false and "foo bar" if {@code someCondition}
 *  is true.
 */
public final class Concat implements CharSequence {
  private final StringBuilder sb;
  
  public Concat() {
    this(new StringBuilder());
  }
  
  public Concat(CharSequence cs) {
    this(new StringBuilder(cs));
  }
  
  public Concat(StringBuilder sb) {
    this.sb = sb;
  }
  
  public Concat append(Object obj) {
    sb.append(obj);
    return this;
  }
  
  public Concat appendArray(String separator, Object... objs) {
    for (int i = 0; i < objs.length; i++) {
      append(objs[i]).when(i < objs.length - 1).append(separator);
    }
    return this;
  }
  
  public ConditionalConcat when(boolean condition) {
    return new ConditionalConcat(condition);
  }
  
  public ConditionalConcat whenIsNull(Object obj) {
    return when(obj == null);
  }
  
  public ConditionalConcat whenIsNotNull(Object obj) {
    return when(obj != null);
  }
  
  public final class ConditionalConcat {
    private final boolean conditionMet;

    ConditionalConcat(boolean conditionMet) {
      this.conditionMet = conditionMet;
    }
    
    public Concat append(Object obj) {
      return conditional(c -> c.append(obj));
    }
    
    public Concat appendArray(String separator, Object... objs) {
      return conditional(c -> c.appendArray(separator, objs));
    }
    
    private Concat conditional(Consumer<Concat> lambda) {
      if (conditionMet) {
        lambda.accept(Concat.this);
      }
      return Concat.this;
    }
  }

  @Override
  public int length() {
    return sb.length();
  }

  @Override
  public char charAt(int index) {
    return sb.charAt(index);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return sb.subSequence(start, end);
  }
  
  @Override
  public String toString() {
    return sb.toString();
  }
}
