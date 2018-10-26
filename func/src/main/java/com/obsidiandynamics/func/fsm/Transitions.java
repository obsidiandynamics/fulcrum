package com.obsidiandynamics.func.fsm;

import static com.obsidiandynamics.func.Functions.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 *  Assists in the construction of finite state automata by enforcing state transition
 *  rules and locating paths between states. 
 *  
 *  @param <S> Elemental state type.
 */
public final class Transitions<S> {
  private final Map<S, Set<S>> allowed = new HashMap<>();
  
  /**
   *  Builder for adding allowed transitions.
   */
  public final class Allow {
    private final S from;
    
    Allow(S from) {
      this.from = from;
    }
    
    /**
     *  Permit transitions to any of the given {@code to} states.
     *  
     *  @param to The allowed <i>to</i> states.
     *  @return The {@link Transitions} instance.
     */
    @SuppressWarnings("unchecked")
    public Transitions<S> to(S... to) {
      addAllowed(from, to);
      return Transitions.this;
    }
  }
  
  /**
   *  Allow a transition.
   *  
   *  @param from The <i>from</i> state.
   *  @return The fluent {@link Allow} rule builder for chaining.
   */
  public Allow allow(S from) {
    return new Allow(from);
  }
  
  private void addAllowed(S from, S[] to) {
    allowed.put(from, Arrays.stream(to).collect(Collectors.toSet()));
  }
  
  /**
   *  Determines whether the given transition is permitted.
   *  
   *  @param transition The transition to check.
   *  @return Whether the transition is allowed.
   */
  public boolean isAllowed(Transition<S> transition) {
    return isAllowed(transition.getFrom(), transition.getTo());
  }
  
  /**
   *  Determines whether the transition {@code from} -&gt; {@code to} is permitted.
   *  
   *  @param from The <i>from</i> state.
   *  @param to The <i>to</i> state.
   *  @return Whether the transition is allowed.
   */
  public boolean isAllowed(S from, S to) {
    if (from.equals(to)) {
      return true;
    } else {
      final Set<S> targets = allowed.get(from);
      if (targets != null) {
        return targets.contains(to);
      } else {
        return false;
      }
    }
  }
  
  /**
   *  Attempts the given transition, returning the <i>to</i> state if allowed.
   *  
   *  @param transition The transition to attempt.
   *  @return The <i>to</i> state.
   *  @throws IllegalTransitionException If the transition is not permitted.
   */
  public S guard(Transition<S> transition) throws IllegalTransitionException {
    return guard(transition.getFrom(), transition.getTo());
  }
  
  /**
   *  Attempts the transition {@code from} -&gt; {@code to}, returning the {@code to} state
   *  if allowed.
   *  
   *  @param from The <i>from</i> state.
   *  @param to The <i>to</i> state.
   *  @return The <i>to</i> state.
   *  @throws IllegalTransitionException If the transition is not permitted.
   */
  public S guard(S from, S to) throws IllegalTransitionException {
    mustBeTrue(isAllowed(from, to), withMessage("Cannot transition " + from + " -> " + to,
                                                IllegalTransitionException::new));
    return to;
  }
  
  /**
   *  Obtains the set of non-recurring states. Specifically, this is the set of <i>from</i> states
   *  that do not appear in any of the target <i>to</i> states. A non-recurring state cannot be
   *  entered from any other state; it can only be assigned as an initial state. <p>
   *  
   *  Note: a non-recurring state is a special case of an initial state in that a non-recurring state
   *  must be a member of a set of initial states; however, an initial state is not necessarily 
   *  non-recurring — some initial states may be freely transitioned in and out of.
   *  
   *  @return The {@link Set} of non-recurring states.
   */
  public Set<S> nonRecurring() {
    final Set<S> toStates = allowed.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
    final Set<S> fromStates = allowed.keySet();
    return fromStates.stream().filter(not(toStates::contains)).collect(Collectors.toSet());
  }
  
  /**
   *  Determines whether the given state is non-recurring, i.e. it cannot be transitioned into from
   *  any other state.
   *  
   *  @param state The state.
   *  @return Whether the given {@code state} is a non-recurring state.
   */
  public boolean isNonRecurring(S state) {
    final Set<S> toStates = allowed.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
    return ! toStates.contains(state);
  }
  
  /**
   *  Obtains the set of terminal states. Specifically, this is the set of <i>to</i> states
   *  that do not appear in any of the <i>from</i> states. <p>
   *  
   *  Note: there may not necessarily be an explicit terminal state if every state can be
   *  transitioned away from.
   *  
   *  @return The {@link Set} of initial states.
   */
  public Set<S> terminal() {
    final Set<S> toStates = allowed.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
    final Set<S> fromStates = allowed.keySet();
    return toStates.stream().filter(not(fromStates::contains)).collect(Collectors.toSet());
  }
  
  /**
   *  Computes the shortest path(s) from the given {@code from} and {@code to} states, assuming
   *  that at least one such path exists (otherwise an empty list is returned). <p>
   *  
   *  A path is a list of intermediate state elements, <i>normally</i> concluded by the final {@code to}
   *  element. (An exception being a path from any state to itself, treated as a single empty path.) 
   *  Multiple paths may be returned if they tie for being the shortest. <p>
   *  
   *  The algorithm employs a recursive, depth-first traversal of the transition tree. It will always
   *  complete in polynomial time, but is constrained by the stack size.
   *
   *  @param from The <i>from</i> state.
   *  @param to The <i>to</i> state.
   *  @return The {@link List} of path {@link List}s.
   */
  public List<List<S>> shortestPaths(S from, S to) {
    if (from.equals(to)) {
      return Collections.singletonList(Collections.emptyList());
    } else {
      return computePath(Collections.emptyList(), from, to);
    }
  }
  
  private List<List<S>> computePath(List<S> path, S current, S to) {
    final Set<S> nextStates = allowed.get(current);
    if (nextStates == null) {
      return Collections.emptyList();
    } else if (nextStates.contains(to)) {
      return Collections.singletonList(join(path, to));
    } else {
      final List<List<S>> paths = new ArrayList<>(nextStates.size());
      for (S nextState : nextStates) {
        if (! path.contains(nextState)) { // this check prevents cyclic recursion
          final List<List<S>> nextPaths = computePath(join(path, nextState), nextState, to);
          paths.addAll(nextPaths);
        }
      }
      
      if (! paths.isEmpty()) {
        return shortest(paths);
      } else {
        return Collections.emptyList();
      }
    }
  }
  
  static <S> List<List<S>> shortest(List<List<S>> lists) {
    final Iterator<List<S>> iterator = lists.iterator();
    final List<List<S>> currentShortest = new ArrayList<>();
    final List<S> firstPath = iterator.next();
    currentShortest.add(firstPath);
    int currentShortestLength = firstPath.size();
    
    while (iterator.hasNext()) {
      final List<S> path = iterator.next();
      final int pathLength = path.size();
      if (pathLength == currentShortestLength) {
        currentShortest.add(path);
      } else if (pathLength < currentShortestLength) {
        currentShortest.clear();
        currentShortest.add(path);
        currentShortestLength = pathLength;
      }
    }
    return currentShortest;
  }
  
  /**
   *  Creates a new list that is a copy of the given {@code head} list, with the {@code tail}
   *  element appended to the end.
   *  
   *  @param head The <i>head</i> list.
   *  @param tail The <i>tail</i> element.
   *  @return The spliced {@link List}.
   */
  private static <S> List<S> join(List<S> head, S tail) {
    final List<S> grown = new ArrayList<>(head.size());
    grown.addAll(head);
    grown.add(tail);
    return grown;
  }
  
  /**
   *  Determines whether the given state can be transitioned from.
   *  
   *  @param state The state.
   *  @return Whether the given {@code state} is a terminal state.
   */
  public boolean isTerminal(S state) {
    final Set<S> fromStates = allowed.keySet();
    return ! fromStates.contains(state);
  }
  
  private static <T> Predicate<T> not(Predicate<T> predicate) {
    return predicate.negate();
  }
  
  @Override
  public int hashCode() {
    return Objects.hashCode(allowed);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof Transitions) {
      return Objects.equals(allowed, ((Transitions<?>) obj).allowed);
    } else {
      return false;
    }
  }
  
  @Override
  public String toString() {
    return Transitions.class.getSimpleName() + " [allowed=" + allowed + "]";
  }
}
