package com.obsidiandynamics.zlg;

public final class SysOutLogService extends PrintStreamLogService {
  public SysOutLogService() {
    super(System.out);
  }
}
