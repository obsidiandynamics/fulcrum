package com.obsidiandynamics.worker;

import java.util.*;

/**
 *  An abstract base class for fluently composing elements into an operational set that (typically)
 *  would take on the properties of each of its underlying members, thereby permitting bulk operations
 *  on the entire set that map to equivalent operations on the members.
 *  
 *  @param <E> The element type.
 *  @param <O> The type of this set for fluent chaining.
 */
public abstract class FluentOperatingSet<E, O extends FluentOperatingSet<E, O>> {
  protected final Set<E> elements = new LinkedHashSet<>();
  
  protected FluentOperatingSet() {}
  
  public final O add(Collection<? extends E> elements) {
    elements.forEach(this::add);
    return self();
  }
  
  @SuppressWarnings("unchecked") 
  public final O add(E... elements) {
    return add(Arrays.asList(elements));
  }
  
  public final O add(E element) {
    elements.add(element);
    return self();
  }
  
  public final O add(Optional<? extends E> elementOpt) {
    elementOpt.ifPresent(this::add);
    return self();
  }
  
  public final O remove(E element) {
    elements.remove(element);
    return self();
  }
  
  public final Collection<E> view() {
    return Collections.unmodifiableCollection(new HashSet<>(elements));
  }
  
  @SuppressWarnings("unchecked")
  private O self() {
    return (O) this;
  }
  
  @Override
  public final String toString() {
    return elements.toString();
  }
}
