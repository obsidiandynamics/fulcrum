package com.obsidiandynamics.func.fsm;

public final class IllegalTransitionException extends Exception {
  private static final long serialVersionUID = 1L;
  
  IllegalTransitionException(String m) { super(m); }
}