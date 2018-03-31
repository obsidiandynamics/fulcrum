package com.obsidiandynamics.jgroups;

import org.jgroups.*;

@FunctionalInterface
public interface HostMessageHandler {
  void handle(JChannel channel, Message message) throws Exception;
}