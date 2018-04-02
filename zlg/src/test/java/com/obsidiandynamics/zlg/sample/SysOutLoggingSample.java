package com.obsidiandynamics.zlg.sample;

import java.util.*;

import com.obsidiandynamics.zlg.*;

public final class SysOutLoggingSample {
  private static final Zlg z = Zlg.forClass(SysOutLoggingSample.class).get();
  
  public static void main(String[] args) {
    z.i("Starting with %d args: %s").arg(args.length).arg(Arrays.asList(args)).log();
    z.w("An error occurred at %s").arg(new Date()).stack(new RuntimeException()).tag("I/O").log();
  }
}
