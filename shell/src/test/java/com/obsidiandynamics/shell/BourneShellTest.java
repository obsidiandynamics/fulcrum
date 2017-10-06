package com.obsidiandynamics.shell;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.shell.BourneShell.*;

public final class BourneShellTest {
  @Test
  public void testWithoutPath() {
    assertArrayEquals(new String[] { "sh", "-c", "a b"},
                      new BourneShell()
                      .withVariant(Variant.SH).prepare("a", "b"));
  }
  
  @Test
  public void testWithPath() {
    assertArrayEquals(new String[] { "sh", "-c", "export PATH=$PATH:/foo && a b"},
                      new BourneShell()
                      .withPath("/foo")
                      .withVariant(Variant.SH).prepare("a", "b"));
  }
}
