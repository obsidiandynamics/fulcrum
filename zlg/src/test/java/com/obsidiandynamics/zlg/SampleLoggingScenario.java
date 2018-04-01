package com.obsidiandynamics.zlg;

import java.util.*;

public final class SampleLoggingScenario {
  private static final Zlg z = Zlg.forClass(SampleLoggingScenario.class).get();
  
  public static void main(String[] args) {
    z.i("Starting with %d args: %s").arg(args.length).arg(Arrays.asList(args)).log();
    z.w("An error occurred at %s").arg(new Date()).stack(new RuntimeException()).log();
  }
}
