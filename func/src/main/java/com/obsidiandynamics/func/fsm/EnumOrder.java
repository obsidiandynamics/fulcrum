package com.obsidiandynamics.func.fsm;

import static com.obsidiandynamics.func.Functions.*;

import java.util.*;

/**
 *  Cements a custom ordering of an enum's elements that may vary from the enum's natural
 *  ordering.
 *  
 *  @param <E> Enum type.
 */
public final class EnumOrder<E extends Enum<E>> implements Iterable<E> {
  private final Class<E> enumType;
  
  private final int[] order;
  
  private EnumOrder(Class<E> enumType, int[] order) {
    this.enumType = enumType;
    this.order = order;
  }
  
  /**
   *  Obtains an array of enums that have been arranged in accordance with this ordering.
   *  
   *  @return An ordered array of enum elements.
   */
  public E[] ordered() {
    final E[] world = enumType.getEnumConstants();
    final E[] ordered = enumType.getEnumConstants(); // copy for mutation (not shared)
    for (E en : world) {
      final int orderIndex = of(en);
      ordered[orderIndex] = en;
    }
    return ordered;
  }

  /**
   *  Obtains the first enum element in the ordering.
   *  
   *  @return The first element.
   */
  public E first() {
    final E[] ordered = ordered();
    return ordered[0];
  }

  /**
   *  Obtains the last enum element in the ordering.
   *  
   *  @return The last element.
   */
  public E last() {
    final E[] ordered = ordered();
    return ordered[ordered.length - 1];
  }
  
  /**
   *  Identifies the highest ordered element in the given collection.
   *  
   *  @param elements The elements to consider.
   *  @return The element with the highest order.
   */
  public E highest(Collection<E> elements) {
    return findExtreme(elements, first(), 1);
  }

  /**
   *  Identifies the lowest ordered element in the given collection.
   *  
   *  @param elements The elements to consider.
   *  @return The element with the lowest order.
   */
  public E lowest(Collection<E> elements) {
    return findExtreme(elements, last(), -1);
  }
  
  private E findExtreme(Collection<E> elements, E initial, int signum) {
    ensureNotEmpty(elements);
    
    E currentExtreme = initial;
    final Comparator<E> comparator = comparator();
    for (E el : elements) {
      if (comparator.compare(el, currentExtreme) == signum) {
        currentExtreme = el;
      }
    }
    return currentExtreme;
  }
  
  private static void ensureNotEmpty(Collection<?> array) {
    mustBeTrue(! array.isEmpty(), illegalArgument("No elements to search"));
  }
  
  @Override
  public Iterator<E> iterator() {
    return Arrays.asList(ordered()).iterator();
  }
  
  /**
   *  Obtains the (zero-based) order of the given enum according to the ordering defined herein.
   *  
   *  @param en The enum.
   *  @return The enum's order.
   */
  public int of(E en) {
    return order[en.ordinal()];
  }
  
  /**
   *  Obtains the comparator for this ordering.
   *  
   *  @return The {@link Comparator}.
   */
  public Comparator<E> comparator() {
    return (s0, s1) -> {
      final int order0 = of(s0);
      final int order1 = of(s1);
      return Integer.compare(order0, order1);
    };
  }
  
  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + Objects.hashCode(enumType);
    result = 31 * result + Arrays.hashCode(order);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof EnumOrder) {
      final EnumOrder<?> that = (EnumOrder<?>) obj;
      return Objects.equals(enumType, that.enumType) && Arrays.equals(order, that.order);
    } else {
      return false;
    }
  }
  
  @Override
  public String toString() {
    return EnumOrder.class.getSimpleName() + " [enumType=" + enumType + ", order=" + Arrays.toString(order) + "]";
  }

  /**
   *  Creates a concrete ordering of an enum type using the given {@code orderedEnums} array. <p>
   *  
   *  The number of elements specified must equal to the number of elements defined in the enum.
   *  Duplicate elements are not permitted.
   *  
   *  @param <E> Enum type.
   *  @param enumType The enum type.
   *  @param orderedEnums The intended order.
   *  @return The corresponding {@link EnumOrder}.
   */
  @SuppressWarnings("unchecked")
  public static <E extends Enum<E>> EnumOrder<E> capture(Class<E> enumType, E... orderedEnums) {
    final E[] world = enumType.getEnumConstants();
    mustBeEqual(world.length, orderedEnums.length, illegalArgument("Order omits one or more required enums"));
    
    final int[] order = new int[world.length];
    for (int i = 0; i < world.length; i++) {
      // every element in the enum's world must be present in orderedEnums, otherwise an exception is thrown
      final int index = indexOfMandatory(world[i], orderedEnums);
      order[i] = index;
    }
    return new EnumOrder<>(enumType, order);
  }
  
  /**
   *  Locates an enum among the given array of enums, returning its index or {@code -1} if the 
   *  enum is not among the given array.
   *  
   *  @param <E> Enum type.
   *  @param en The enum to find.
   *  @param enums The enums to search in.
   *  @return The index, or {@code -1} if the enum couldn't be found.
   */
  public static <E extends Enum<E>> int indexOf(E en, E[] enums) {
    for (int i = 0; i < enums.length; i++) {
      if (enums[i] == en) {
        return i;
      }
    }
    return -1;
  }
  
  /**
   *  Locates an enum among the given array of enums, where it is assumed that the enum must be
   *  present. Returns the index of the enum, or throws an {@link IllegalArgumentException} if it 
   *  couldn't be found.
   *  
   *  @param <E> Enum type.
   *  @param en The enum to find.
   *  @param enums The enums to search in.
   *  @return The index.
   *  @throws IllegalArgumentException If the enum could not be found.
   */
  public static <E extends Enum<E>> int indexOfMandatory(E en, E[] enums) {
    final int index = indexOf(en, enums);
    mustBeTrue(index != -1, 
        withMessage(() -> en + " is not among " + Arrays.toString(enums), IllegalArgumentException::new));
    return index;
  }
}
