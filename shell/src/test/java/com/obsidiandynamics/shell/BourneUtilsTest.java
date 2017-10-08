package com.obsidiandynamics.shell;

import static org.junit.Assert.*;

import org.junit.*;

import com.obsidiandynamics.assertion.*;

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
  public void testRun() {
    final StringBuilder sink = new StringBuilder();
    BourneUtils.run("echo hello", null, false, sink::append);
    assertEquals("hello\n", sink.toString());
  }

  @Test
  public void testRunVerbose() {
    final StringBuilder sink = new StringBuilder();
    BourneUtils.run("echo hello", null, true, sink::append);
    assertEquals("$ sh -c echo hello\nhello\n", sink.toString());
  }
}
