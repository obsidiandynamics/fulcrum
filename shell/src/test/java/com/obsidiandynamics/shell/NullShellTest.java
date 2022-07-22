package com.obsidiandynamics.shell;

import static org.junit.Assert.*;

import org.junit.*;

public final class NullShellTest {
  private final NullShell shell = NullShell.getIntance();
  
  @Test
  public void test() {
    assertArrayEquals(new String[] { "a", "b"},
                      shell.prepare(new String[] { "a", "b" }));
  }
  
  @Test
  public void testDefaultEcho() {
    assertEquals("foo bar", shell.getDefaultEcho().apply(new String[] { "foo", "bar" }));
  }
}
