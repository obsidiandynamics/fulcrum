package com.obsidiandynamics.jgroups;

import java.util.*;

import org.jgroups.*;

@FunctionalInterface
public interface GroupMessageHandler {
  void handle(JChannel channel, Map<Address, Message> messages) throws Exception;
}