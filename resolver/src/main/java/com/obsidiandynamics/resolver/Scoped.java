package com.obsidiandynamics.resolver;

import java.util.*;
import java.util.function.*;

/**
 *  Provides a lookup map that is constrained to a particular scope.
 */
@FunctionalInterface
public interface Scoped {
  Map<Class<?>, Supplier<Object>> map();
}
