package com.obsidiandynamics.shell;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;

public final class DefaultProcessExecutorTest {
  @Test
  public void test() throws IOException, InterruptedException {
    final Process proc = DefaultProcessExecutor.getInstance().run(new String[] { "cat", "src/test/sh/exit-code.sh" });
    final int exitCode = proc.waitFor();
    assertEquals(0, exitCode);
  }
}
