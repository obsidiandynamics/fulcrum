package com.obsidiandynamics.jgroups;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.jgroups.*;
import org.jgroups.Message.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.util.Util.*;

import com.obsidiandynamics.func.*;

public final class Group implements AutoCloseable {
  private final JChannel channel;
  
  private final Set<HostMessageHandler> generalHandlers = new CopyOnWriteArraySet<>();
  
  private final ConcurrentMap<Serializable, Set<HostMessageHandler>> idHandlers = new ConcurrentHashMap<>();
  
  private LogLine debug = LogLine.nop();
  
  private ExceptionHandler errorHandler = ExceptionHandler.forPrintStream(System.err);
      
  public Group(JChannel channel) throws Exception {
    this.channel = channel;
    channel.setDiscardOwnMessages(true);
    channel.setReceiver(new ReceiverAdapter() {
      @Override public void receive(Message msg) {
        debug.printf("Received %s", msg);
        try {
          for (HostMessageHandler onMessage : generalHandlers) {
            onMessage.handle(channel, msg);
          }
          
          final Object payload = msg.getObject();
          if (payload instanceof SyncPacket) {
            final SyncPacket syncMessage = (SyncPacket) payload;
            final Set<HostMessageHandler> handlers = idHandlers.get(syncMessage.getId());
            if (handlers != null) {
              for (HostMessageHandler handler : handlers) {
                handler.handle(channel, msg);
              }
            }
          }
        } catch (Throwable e) {
          errorHandler.onException(String.format("Exception processing message %s", msg), e);
        }
      }
    });
  }
  
  public Group withDebug(LogLine debug) {
    this.debug = debug;
    return this;
  }
  
  public Group withErrorHandler(ExceptionHandler errorHandler) {
    this.errorHandler = errorHandler;
    return this;
  }
  
  public Group withMessageHandler(HostMessageHandler handler) {
    generalHandlers.add(handler);
    return this;
  }  
  
  public int numMessageHandlers() {
    return generalHandlers.size();
  }
  
  public void removeMessageHandler(HostMessageHandler handler) {
    generalHandlers.remove(handler);
  }
  
  public Group withMessageHandler(Serializable id, HostMessageHandler handler) {
    idHandlers.computeIfAbsent(id, key -> new CopyOnWriteArraySet<>()).add(handler);
    return this;
  }  
  
  public void send(Message message) throws Exception {
    channel.send(message);
  }
  
  public int numMessageHandlers(Serializable id) {
    return idHandlers.getOrDefault(id, Collections.emptySet()).size();
  }
  
  public void removeMessageHandler(Serializable id, HostMessageHandler handler) {
    idHandlers.computeIfPresent(id, (key, handlers) -> {
      handlers.remove(handler);
      return handlers.isEmpty() ? null : handlers;
    });
  }
  
  public CompletableFuture<Message> request(Address address, SyncPacket syncMessage, Flag... flags) throws Exception {
    final CompletableFuture<Message> f = new CompletableFuture<>();
    final ResponseSync rs = request(address, syncMessage, (channel, message) -> {
      f.complete(message);
    }, flags);
    f.whenComplete((message, throwable) -> {
      if (f.isCancelled()) {
        rs.cancel();
      }
    });
    return f;
  }
  
  public ResponseSync request(Address address, SyncPacket syncMessage, HostMessageHandler handler, Flag... flags) throws Exception {
    final Serializable id = syncMessage.getId();
    final HostMessageHandler idHandler = new HostMessageHandler() {
      @Override public void handle(JChannel channel, Message resp) throws Exception {
        removeMessageHandler(id, this);
        handler.handle(channel, resp);
      }
    };
    withMessageHandler(id, idHandler);
    channel.send(new Message(null, syncMessage).setFlag(flags));
    return new ResponseSync(this, id, idHandler);
  }
  
  public CompletableFuture<Map<Address, Message>> gather(SyncPacket syncMessage, Flag... flags) throws Exception {
    return gather(channel.getView().size() - 1, syncMessage, flags);
  }
  
  public CompletableFuture<Map<Address, Message>> gather(int respondents, SyncPacket syncMessage, Flag... flags) throws Exception {
    final CompletableFuture<Map<Address, Message>> f = new CompletableFuture<>();
    final ResponseSync rs = gather(respondents, syncMessage, (channel, messages) -> {
      f.complete(messages);
    }, flags);
    f.whenComplete((message, throwable) -> {
      if (f.isCancelled()) {
        rs.cancel();
      }
    });
    return f;
  }
  
  public ResponseSync gather(SyncPacket syncMessage, GroupMessageHandler handler, Flag... flags) throws Exception {
    return gather(channel.getView().size() - 1, syncMessage, handler, flags);
  }
  
  public ResponseSync gather(int respondents, SyncPacket syncMessage, GroupMessageHandler handler, Flag... flags) throws Exception {
    final Map<Address, Message> responses = new ConcurrentHashMap<>();
    final Serializable id = syncMessage.getId();
    final HostMessageHandler idHandler = new HostMessageHandler() {
      @Override public void handle(JChannel channel, Message resp) throws Exception {
        responses.put(resp.getSrc(), resp);
        if (responses.size() == respondents) {
          removeMessageHandler(id, this);
          handler.handle(channel, Collections.unmodifiableMap(responses));
        }
      }
    };
    withMessageHandler(id, idHandler);
    channel.send(new Message(null, syncMessage).setFlag(flags));
    return new ResponseSync(this, id, idHandler);
  }
  
  public Group connect(String clusterName) throws Exception {
    channel.connect(clusterName);
    return this;
  }
  
  public JChannel channel() {
    return channel;
  }
  
  public View view() {
    return channel.view();
  }
  
  public Set<Address> peers() {
    final Address current = channel.getAddress();
    final Set<Address> addresses = new HashSet<>(channel.view().getMembers());
    addresses.remove(current);
    return addresses;
  }
  
  public Address peer() {
    return peers().iterator().next();
  }
  
  /**
   *  Closes this group, including the underlying {@link JChannel}.
   */
  @Override
  public void close() {
    channel.close();
  }
  
  /**
   *  Creates a new UDP-based {@link JChannel}.
   *  
   *  @param bindAddress The address to bind to, or {@code null} to bind to the default external interface.
   *                     Note: you may consider setting {@code -Djava.net.preferIPv4Stack=true} if binding
   *                     to an external interface.
   *  @return A new channel.
   *  @throws Exception If an error occurs.
   *  
   *  @see org.jgroups.util.Util#getAddress(AddressScope) for specifying one of the predefined
   *  address scopes {@code [GLOBAL, SITE_LOCAL, LINK_LOCAL, NON_LOOPBACK]}.
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
                        new FRAG2());
  }
  
  /**
   *  Creates a new VM-local loopback channel.
   *  
   *  @return A new channel.
   *  @throws Exception Exception If an error occurs.
   */
  public static JChannel newLoopbackChannel() throws Exception {
    return new JChannel(new SHARED_LOOPBACK(),
                        new SHARED_LOOPBACK_PING(),
                        new MERGE3(),
                        new FD_ALL(),
                        new VERIFY_SUSPECT(),
                        new BARRIER(),
                        new NAKACK2(),
                        new UNICAST3(),
                        new STABLE(),
                        createGMS(),
                        new UFC(),
                        new MFC(),
                        new FRAG2());
  }
  
  private static GMS createGMS() {
    final GMS gms = new GMS();
    gms.setPrintLocalAddr(false);
    return gms;
  }
}
