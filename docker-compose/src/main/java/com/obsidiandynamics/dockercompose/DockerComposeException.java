package com.obsidiandynamics.dockercompose;

public final class DockerComposeException extends Exception {
  private static final long serialVersionUID = 1L;
  
  DockerComposeException(String command, String output, int code) { 
    super(String.format("Command '%s' terminated with code %d, output: %s", command, code, output)); 
  }
}