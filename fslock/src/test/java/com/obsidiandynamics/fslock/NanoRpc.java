package com.obsidiandynamics.fslock;

import static com.obsidiandynamics.func.Functions.*;

import java.net.*;
import java.util.*;

import io.undertow.*;
import io.undertow.Undertow.*;
import io.undertow.server.*;
import io.undertow.server.handlers.*;
import io.undertow.server.handlers.accesslog.*;
import io.undertow.util.*;

/**
 *  A miniaturised web server supporting RPC-style interactions. A single POST '/' endpoint
 *  delegates to the supplied blocking {@link HttpHandler}.
 */
public final class NanoRpc {
  private static final int NUM_PROCESSORS = Runtime.getRuntime().availableProcessors();
  
  private final HttpHandler handler;
  
  /** Leaving the default value of {@code 0} results in automatic assignment from the ephemeral range. */
  private int port;
  
  private HttpString method = Methods.POST;
  
  private Undertow undertow;
  
  public NanoRpc(HttpHandler handler) {
    this.handler = mustExist(handler, "Handler cannot be null");
  }
  
  public NanoRpc withPort(int port) {
    this.port = port;
    return this;
  }

  public NanoRpc withMethod(HttpString method) {
    this.method = mustExist(method, "Method cannot be null");
    return this;
  }
  
  public NanoRpc start() {
    mustBeNull(undertow, IllegalStateException::new);
    
    final HttpHandler wrappedHandler = exchange -> {
      try {
        handler.handleRequest(exchange);
      } catch (Throwable e) {
        System.err.println("Exception in handler");
        e.printStackTrace();
        exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
        exchange.setReasonPhrase(e.getMessage());
        exchange.endExchange();
      }
    };
    final RoutingHandler routingHandler = Handlers.routing()
        .add(method, "/", new BlockingHandler(wrappedHandler));
    final HttpHandler rootHandler = new AccessLogHandler(routingHandler,
                                                         System.out::println,
                                                         "combined",
                                                         NanoRpc.class.getClassLoader());
    undertow = Undertow.builder()
        .addHttpListener(port, "0.0.0.0")
        .setIoThreads(NUM_PROCESSORS)
        .setDirectBuffers(false)
        .setWorkerThreads(NUM_PROCESSORS * 8)
        .setHandler(rootHandler)
        .build();

    undertow.start();
    return this;
  }
  
  public void stop() {
    if (undertow != null) {
      undertow.getWorker().shutdownNow();
      undertow.stop();
      undertow = null;
    }
  }
  
  public int getBoundPort() {
    if (undertow != null) {
      final List<ListenerInfo> listenerInfoList = undertow.getListenerInfo();
      return ((InetSocketAddress) listenerInfoList.get(0).getAddress()).getPort();
    } else {
      return 0;
    }
  }
}