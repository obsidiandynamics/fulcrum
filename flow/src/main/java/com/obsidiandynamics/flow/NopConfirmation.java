package com.obsidiandynamics.flow;

public final class NopConfirmation implements Confirmation {
  private static final NopConfirmation instance = new NopConfirmation();
  
  public static NopConfirmation getInstance() {
    return instance;
  }

  private NopConfirmation() {}
  
  @Override
  public void confirm() {}
}
