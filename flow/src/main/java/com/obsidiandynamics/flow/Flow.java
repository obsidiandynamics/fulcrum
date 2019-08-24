package com.obsidiandynamics.flow;

import java.util.*;

import com.obsidiandynamics.worker.*;

public interface Flow extends Terminable, Joinable {
  StatefulConfirmation begin(Object id, Runnable task);
  
  Map<Object, StatefulConfirmation> getPendingConfirmations();
}
