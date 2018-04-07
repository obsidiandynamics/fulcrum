package com.obsidiandynamics.resolver;

import java.util.*;
import java.util.function.*;

@FunctionalInterface
public interface Scoped {
  Map<Class<?>, Supplier<? extends Object>> get();
}
