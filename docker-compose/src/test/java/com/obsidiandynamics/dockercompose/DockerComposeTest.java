package com.obsidiandynamics.dockercompose;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.io.*;

import org.hamcrest.*;
import org.hamcrest.collection.*;
import org.junit.*;

import com.obsidiandynamics.shell.*;

public final class DockerComposeTest {
  private ProcessExecutor executor;

  private DockerCompose compose;

  private Process process;

  @Before
  public void before() throws IOException {
    executor = mock(ProcessExecutor.class);
    compose = new DockerCompose().withExecutor(executor).withShell(new NullShell());
    process = mock(Process.class);
    when(executor.run(any())).thenReturn(process);
    when(process.getInputStream()).thenReturn(new ByteArrayInputStream("test output".getBytes()));
  }

  @Test
  public void testCheckInstalledPass() throws InterruptedException {
    when(process.waitFor()).thenReturn(0);
    compose.checkInstalled();
  }

  @Test(expected=NotInstalledError.class)
  public void testCheckInstalledFail() throws InterruptedException {
    when(process.waitFor()).thenReturn(3);
    compose.checkInstalled();
  }

  @Test(expected=IllegalStateException.class)
  public void testEnsureComposeFileAssigned() throws DockerComposeException {
    compose.up();
  }

  @Test(expected=DockerComposeException.class)
  public void testUpFailure() throws InterruptedException, DockerComposeException {
    when(process.waitFor()).thenReturn(3);
    compose.withComposeFile("file").up();
  }

  @SafeVarargs
  private static <T> IsArray<T> isArray(Matcher<T>... elementMatchers) {
    return IsArray.array(elementMatchers);
  }

  @Test
  public void testUp() throws DockerComposeException, IOException {
    compose.withComposeFile("file").up();
    verify(executor).run(argThat(isArray(allOf(startsWith("docker-compose "),
                                               containsString(" -f file"),
                                               containsString(" up"),
                                               containsString(" -d")))));
  }

  @Test
  public void testUpWithSinkEcho() throws DockerComposeException, IOException {
    final StringBuilder sink = new StringBuilder();
    compose.withComposeFile("file").withEcho(true).withSink(sink::append).up();
    verify(executor).run(argThat(isArray(allOf(startsWith("docker-compose "),
                                               containsString(" -f file"),
                                               containsString(" up"),
                                               containsString(" -d")))));
    assertTrue("sink=" + sink, sink.toString().startsWith("docker-compose "));
    assertTrue("sink=" + sink, sink.toString().contains("test output\n"));
  }

  @Test
  public void testUpWithProject() throws DockerComposeException, IOException {
    compose.withComposeFile("file").withProject("project").up();
    verify(executor).run(argThat(isArray(allOf(startsWith("docker-compose "),
                                               containsString(" -f file"),
                                               containsString(" up"),
                                               containsString(" -p project"),
                                               containsString(" -d")))));
  }

  @Test
  public void testDown() throws DockerComposeException, IOException {
    compose.withComposeFile("file").down(false);
    verify(executor).run(argThat(isArray(allOf(startsWith("docker-compose "),
                                               containsString(" -f file"),
                                               containsString(" down")))));
  }

  @Test
  public void testDownRemoveVolumes() throws DockerComposeException, IOException {
    compose.withComposeFile("file").down(true);
    verify(executor).run(argThat(isArray(allOf(startsWith("docker-compose "),
                                               containsString(" -f file"),
                                               containsString(" down"),
                                               containsString(" -v")))));
  }

  @Test
  public void testStop() throws DockerComposeException, IOException {
    compose.withComposeFile("file").stop(0);
    verify(executor).run(argThat(isArray(allOf(startsWith("docker-compose "),
                                               containsString(" -f file"),
                                               containsString(" stop")))));
  }

  @Test
  public void testStopTimeout() throws DockerComposeException, IOException {
    compose.withComposeFile("file").stop(1000);
    verify(executor).run(argThat(isArray(allOf(startsWith("docker-compose "),
                                               containsString(" -f file"),
                                               containsString(" stop"),
                                               containsString(" -t 1000")))));
  }

  @Test
  public void testRm() throws DockerComposeException, IOException {
    compose.withComposeFile("file").rm(false);
    verify(executor).run(argThat(isArray(allOf(startsWith("docker-compose "),
                                               containsString(" -f file"),
                                               containsString(" rm -f")))));
  }

  @Test
  public void testRmRemoveVolumes() throws DockerComposeException, IOException {
    compose.withComposeFile("file").rm(true);
    verify(executor).run(argThat(isArray(allOf(startsWith("docker-compose "),
                                               containsString(" -f file"),
                                               containsString(" rm -f"),
                                               containsString(" -v")))));
  }
}
