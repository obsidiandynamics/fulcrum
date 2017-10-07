package com.obsidiandynamics.dockercompose;

import org.junit.*;

import com.obsidiandynamics.shell.*;

public final class DockerComposeIT {
  @BeforeClass
  public static void beforeClass() {
    new DockerCompose()
    .withShell(new BourneShell().withPath("/usr/local/bin"))
    .checkInstalled();
  }
  
  @Test
  public void testUpDown() throws DockerComposeException {
    final DockerCompose compose = new DockerCompose()
        .withShell(new BourneShell().withPath("/usr/local/bin"))
        .withComposeFile("stacks/java8/docker-compose.yaml")
        .withProject("test-docker-compose")
        .withEcho(true)
        .withSink(System.out::println);
    
    compose.up();
    compose.down(true);
  }
}
