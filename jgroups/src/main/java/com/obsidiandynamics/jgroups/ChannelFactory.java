package com.obsidiandynamics.jgroups;

import org.jgroups.*;

@FunctionalInterface
public interface ChannelFactory {
  JChannel create() throws Exception;
}