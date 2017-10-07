package com.obsidiandynamics.shell;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.shell.BourneShell.*;

public final class BourneShellTest {
  private BourneShell shell;
  
  @Before
  public void before() {
    shell = new BourneShell();
  }
  
  @Test
  public void testWithoutPath() {
    assertArrayEquals(new String[] { "sh", "-c", "a b"},
                      shell.withVariant(Variant.SH).prepare("a", "b"));
  }
  
  @Test
  public void testWithPath() {
    assertArrayEquals(new String[] { "sh", "-c", "export PATH=$PATH:/foo && a b"},
                      shell.withPath("/foo").withVariant(Variant.SH).prepare("a", "b"));
  }
  
  @Test
  public void testDefaultEcho() {
    assertEquals("$ foo bar", shell.getDefaultEcho().apply(new String[] { "foo", "bar" }));
  }
}
