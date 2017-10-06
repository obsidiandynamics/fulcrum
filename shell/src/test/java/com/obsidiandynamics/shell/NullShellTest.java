package com.obsidiandynamics.shell;

import static org.junit.Assert.*;

import org.junit.*;

public final class NullShellTest {
  @Test
  public void test() {
    assertArrayEquals(new String[] { "a", "b"},
                      new NullShell().prepare("a", "b"));
  }
}
