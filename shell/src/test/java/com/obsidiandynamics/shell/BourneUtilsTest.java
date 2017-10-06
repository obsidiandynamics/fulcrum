package com.obsidiandynamics.shell;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;
import com.obsidiandynamics.shell.BourneUtils.*;

public final class BourneUtilsTest {
  @Before
  public void before() {
    Assume.assumeTrue(BourneUtils.isShellAvailable());
  }
  
  @Test
  public void testConformance() throws Exception {
    Assertions.assertUtilityClassWellDefined(BourneUtils.class);
  }

  @Test
  public void testRunVerbose() {
    final StringBuilder sink = new StringBuilder();
    BourneUtils.runVerbose("echo hello", null, sink::append);
    assertEquals("$ echo hello\nhello\n", sink.toString());
  }
  
  @Test
  public void testIsNotInstalled() throws CommandExecutionException {
    assertFalse(BourneUtils.isInstalled("xyz_not_found", null));
  }
  
  @Test
  public void testIsInstalled() throws CommandExecutionException {
    assertTrue(BourneUtils.isInstalled("ls src/test/sh", null));
  }
  
  @Test(expected=CommandExecutionException.class)
  public void testIsInstalledError() throws CommandExecutionException {
    BourneUtils.isInstalled("src/test/sh/exit-code.sh 3", null);
  }
  
  @Test
  public void testAssertInstalled() throws CommandExecutionException {
    BourneUtils.assertInstalled("XYZ", "ls src/test/sh", null);
  }
  
  @Test
  public void testAssertNotInstalled() throws CommandExecutionException {
    try {
      BourneUtils.assertInstalled(null, "xyz_not_found", "XYZ");
      fail("Failed to throw an " + NotInstalledError.class.getName());
    } catch (NotInstalledError e) {}
  }
  
  @Test
  public void testIsSuccess() {
    assertTrue(BourneUtils.isSuccess(0));
    assertFalse(BourneUtils.isSuccess(1));
  }
  
  @Test
  public void isNotFoundOrNotExecutable() {
    assertTrue(BourneUtils.isNotFoundOrNotExecutable(126));
    assertTrue(BourneUtils.isNotFoundOrNotExecutable(127));
    assertFalse(BourneUtils.isNotFoundOrNotExecutable(128));
  }
}
