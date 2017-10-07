package com.obsidiandynamics.dockercompose;

import org.junit.*;

import com.obsidiandynamics.shell.*;

public final class DockerComposeIT {
  @Test
  public void test() throws DockerComposeException {
    final DockerCompose compose = new DockerCompose()
        .withShell(new BourneShell().withPath("/usr/local/bin"))
        .withComposeFile("stacks/java8/docker-compose.yaml")
        .withProject("test-docker-compose")
        .withSink(System.out::println);
    
    compose.up();
    compose.down(true);
  }
}
