package com.obsidiandynamics.dockercompose;

import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.*;

import org.junit.*;

import com.obsidiandynamics.dockercompose.DockerCompose.*;
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
  
  private static String conjunction(String... matchers) {
    if (matchers.length < 2) {
      throw new IllegalArgumentException("matchers.length must be >= 2");
    } else if (matchers.length == 2) {
      return and(matchers[0], matchers[1]);
    } else {
      final String[] tail = new String[matchers.length - 1];
      System.arraycopy(matchers, 1, tail, 0, tail.length);
      return and(matchers[0], conjunction(tail));
    }
  }
  
  @Test(expected=DockerComposeException.class)
  public void testUpFailure() throws InterruptedException, DockerComposeException {
    when(process.waitFor()).thenReturn(3);
    compose.withComposeFile("file").up();
  }
  
  @Test
  public void testUp() throws DockerComposeException, IOException {
    compose.withComposeFile("file").up();
    verify(executor).run(conjunction(startsWith("docker-compose "),
                                     contains(" -f file"),
                                     contains(" up"),
                                     contains(" -d")));
  }
  
  @Test
  public void testUpWithSink() throws DockerComposeException, IOException {
    final StringBuilder sink = new StringBuilder();
    compose.withComposeFile("file").withSink(sink::append).up();
    verify(executor).run(conjunction(startsWith("docker-compose "),
                                     contains(" -f file"),
                                     contains(" up"),
                                     contains(" -d")));
    assertEquals("test output\n", sink.toString());
  }
  
  @Test
  public void testUpWithProject() throws DockerComposeException, IOException {
    compose.withComposeFile("file").withProject("project").up();
    verify(executor).run(conjunction(startsWith("docker-compose "),
                                     contains(" -f file"),
                                     contains(" up"),
                                     contains(" -p project"),
                                     contains(" -d")));
  }
  
  @Test
  public void testDown() throws DockerComposeException, IOException {
    compose.withComposeFile("file").down(false);
    verify(executor).run(conjunction(startsWith("docker-compose "),
                                     contains(" -f file"),
                                     contains(" down")));
  }
  
  @Test
  public void testDownRemoveVolumes() throws DockerComposeException, IOException {
    compose.withComposeFile("file").down(true);
    verify(executor).run(conjunction(startsWith("docker-compose "),
                                     contains(" -f file"),
                                     contains(" down"),
                                     contains(" -v")));
  }
  
  @Test
  public void testStop() throws DockerComposeException, IOException {
    compose.withComposeFile("file").stop(0);
    verify(executor).run(conjunction(startsWith("docker-compose "),
                                     contains(" -f file"),
                                     contains(" stop")));
  }
  
  @Test
  public void testStopTimeout() throws DockerComposeException, IOException {
    compose.withComposeFile("file").stop(1000);
    verify(executor).run(conjunction(startsWith("docker-compose "),
                                     contains(" -f file"),
                                     contains(" stop"),
                                     contains(" -t 1000")));
  }
  
  @Test
  public void testRm() throws DockerComposeException, IOException {
    compose.withComposeFile("file").rm(false);
    verify(executor).run(conjunction(startsWith("docker-compose "),
                                     contains(" -f file"),
                                     contains(" rm -f")));
  }
  
  @Test
  public void testRmRemoveVolumes() throws DockerComposeException, IOException {
    compose.withComposeFile("file").rm(true);
    verify(executor).run(conjunction(startsWith("docker-compose "),
                                     contains(" -f file"),
                                     contains(" rm -f"),
                                     contains(" -v")));
  }
}
