package com.obsidiandynamics.shell;

import static org.junit.Assert.*;

import org.junit.*;

public final class NullShellTest {
  private NullShell shell;
  
  @Before
  public void before() {
    shell = new NullShell();
  }
  
  @Test
  public void test() {
    assertArrayEquals(new String[] { "a", "b"},
                      shell.prepare("a", "b"));
  }
  
  @Test
  public void testDefaultEcho() {
    assertEquals("foo bar", shell.getDefaultEcho().apply(new String[] { "foo", "bar" }));
  }
}
