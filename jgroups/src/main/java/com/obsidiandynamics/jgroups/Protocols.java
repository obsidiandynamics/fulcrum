package com.obsidiandynamics.jgroups;

import java.net.*;
import java.util.*;

import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.*;
import org.jgroups.util.*;
import org.jgroups.util.Util.*;

import com.obsidiandynamics.func.*;

/**
 *  Utilities for working with channels and protocol stacks.
 */
public final class Protocols {
  private Protocols() {}
  
  /**
   *  Creates a new UDP-based {@link JChannel}.
   *  
   *  @param bindAddress The address to bind to, or {@code null} to bind to the default external interface
   *                     (which is also equivalent to {@link AddressScope#NON_LOOPBACK}).
   *                     Note: you may need to set {@code -Djava.net.preferIPv4Stack=true} if binding
   *                     to an external interface with an IP v4 address.
   *  @return A new channel.
   *  @throws Exception If an error occurs.
   *  
   *  @see org.jgroups.util.Util#getAddress(AddressScope, StackType) for specifying one of the predefined
   *  address scopes {@code [GLOBAL, SITE_LOCAL, LINK_LOCAL, NON_LOOPBACK, LOOPBACK]}.
   */
  public static JChannel newUdpChannel(InetAddress bindAddress) throws Exception {
    return new JChannel(new UDP().setValue("bind_addr", bindAddress),
                        new PING(),
                        new MERGE3(),
                        new FD_SOCK(),
                        new FD_ALL(),
                        new VERIFY_SUSPECT(),
                        new BARRIER(),
                        new NAKACK2(),
                        new UNICAST3(),
                        new STABLE(),
                        createGMS(),
                        new UFC(),
                        new MFC(),
                        new FRAG2(),
                        new RSVP(),
                        new STATE_TRANSFER());
  }
  
  /**
   *  Creates a new VM-local loopback {@link JChannel}.
   *  
   *  @return A new channel.
   *  @throws Exception Exception If an error occurs.
   */
  public static JChannel newLoopbackChannel() throws Exception {
    return new JChannel(new SHARED_LOOPBACK(),
                        new SHARED_LOOPBACK_PING(),
                        new MERGE3(),
                        new VERIFY_SUSPECT(),
                        new BARRIER(),
                        new NAKACK2(),
                        new UNICAST3(),
                        new STABLE(),
                        createGMS(),
                        new UFC(),
                        new MFC(),
                        new FRAG2(),
                        new STATE_TRANSFER());
  }
  
  private static GMS createGMS() {
    final GMS gms = new GMS();
    gms.setPrintLocalAddr(false);
    return gms;
  }
  
  /**
   *  Finds a protocol in the given stack.
   *  
   *  @param <P> Protocol type.
   *  @param protocolStack The stack to search in.
   *  @param protocolType The protocol class type.
   *  @return An {@link Optional} value encapsulating a {@link Protocol} instance.
   */
  public static <P extends Protocol> Optional<P> findProtocol(ProtocolStack protocolStack, Class<P> protocolType) {
    return Classes.cast(protocolStack.getProtocols().stream().filter(protocolType::isInstance).findAny());
  }
}
