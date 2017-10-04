package com.obsidiandynamics.shell;

public final class RunUlimit {
  public static void main(String[] args) {
    BourneUtils.run("ulimit -Sa", System.out::println);
  }
}
