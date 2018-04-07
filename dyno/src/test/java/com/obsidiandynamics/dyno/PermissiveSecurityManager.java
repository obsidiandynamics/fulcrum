package com.obsidiandynamics.dyno;

import java.security.*;

public final class PermissiveSecurityManager extends SecurityManager {
  @Override
  public void checkPermission(Permission perm) {}
}
