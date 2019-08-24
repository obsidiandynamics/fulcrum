package com.obsidiandynamics.flow;

import static com.obsidiandynamics.func.Functions.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public abstract class AbstractFlow implements Flow {
  private final AtomicReference<StatefulConfirmation> tail = new AtomicReference<>(StatefulConfirmation.anchor());
  
  private final ConcurrentHashMap<Object, StatefulConfirmation> confirmations = new ConcurrentHashMap<>();
  
  protected final FiringStrategy firingStrategy;

  protected AbstractFlow(FiringStrategy.Factory firingStrategyFactory) {
    mustExist(firingStrategyFactory, "Firing strateg factory cannot be null");
    firingStrategy = firingStrategyFactory.create(this, tail.get());
  }
  
  @Override
  public final StatefulConfirmation begin(Object id, Runnable task) {
    mustExist(id, "ID cannot be null");
    mustExist(task, "Task cannot be null");
    final StatefulConfirmation confirmation = confirmations.computeIfAbsent(id, __ -> {
      final StatefulConfirmation newConfirmation = new StatefulConfirmation(id, task, this::fire);
      newConfirmation.appendTo(tail);
      return newConfirmation;
    });
    confirmation.addRequest();
    return confirmation;
  }
  
  abstract void fire();
  
  final void removeWithoutCompleting(Object id) {
    confirmations.remove(id);
  }
  
  final void complete(StatefulConfirmation confirmation) {
    confirmations.remove(confirmation.getId());
    confirmation.getTask().run();
  }
 
  @Override
  public final Map<Object, StatefulConfirmation> getPendingConfirmations() {
    return Collections.unmodifiableMap(confirmations);
  }
}
