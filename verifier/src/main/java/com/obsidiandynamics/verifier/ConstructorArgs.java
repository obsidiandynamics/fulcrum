package com.obsidiandynamics.verifier;

import java.util.*;

import pl.pojo.tester.api.*;

public final class ConstructorArgs {
  private final List<Class<?>> paramTypes = new ArrayList<>();
  
  private final List<Object> args = new ArrayList<>();
  
  public ConstructorArgs() {}
  
  public <T> ConstructorArgs with(Class<? super T> paramType, T arg) {
    paramTypes.add(paramType);
    args.add(arg);
    return this;
  }
  
  ConstructorParameters toConstructorParameters() {
    return new ConstructorParameters(args.toArray(), 
                                     paramTypes.toArray(new Class[paramTypes.size()]));
  }

  @Override
  public String toString() {
    return ConstructorArgs.class.getSimpleName() + " [paramTypes=" + paramTypes + ", args=" + args + "]";
  }
}
