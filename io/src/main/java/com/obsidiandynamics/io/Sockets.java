package com.obsidiandynamics.io;

import static com.obsidiandynamics.func.Functions.*;

import java.io.*;
import java.net.*;

/**
 *  Utilities for working with TCP sockets.
 */
public final class Sockets {
  private Sockets() {}
  
  public static final class PortRangeExhaustedException extends Exception {
    private static final long serialVersionUID = 1L;
    
    PortRangeExhaustedException(String m) { super(m); }
  }
  
  /**
   *  Obtains a spare port (that may subsequently be bound to) from within a specified port range
   *  by randomly probing the ports in the range (starting with one random probe, then cycling 
   *  sequentially through the range and wrapping around until the range is exhausted). <p>
   *  
   *  The availability of the port is a point-in-time assertion and is subject to race conditions. 
   *  By the time this method returns with an available port, the latter may have been bound to 
   *  by another thread within this process, or indeed within any other process. To significantly
   *  minimise the likelihood of a port takeover, specify a range that doesn't overlap with the
   *  ephemeral port range (typically 32768 and above).
   *  
   *  @param rangeFromIncl The lower bound on the port range (inclusive).
   *  @param rangeToExcl The upper bound on the port range (exclusive).
   *  @return One (random) free port in the range.
   *  @throws PortRangeExhaustedException If the range was exhausted.
   */
  public static int getSparePort(int rangeFromIncl, int rangeToExcl) throws PortRangeExhaustedException {
    mustBeTrue(rangeFromIncl >= 1024, withMessage(() -> "Lower port range must include 1024 or higher", IllegalArgumentException::new));
    mustBeTrue(rangeFromIncl < rangeToExcl, withMessage(() -> "Port range cannot overlap", IllegalArgumentException::new));
    mustBeTrue(rangeToExcl <= 65536, withMessage(() -> "Upper port range must exclude 65536 or lower", IllegalArgumentException::new));
    final int randomPort = randomInRange(rangeFromIncl, rangeToExcl);
    
    for (int currentPort = randomPort;;) {
      final boolean currentAvailable = isLocalPortAvailable(currentPort);
      if (currentAvailable) {
        return currentPort;
      }
      
      final int next = nextInRange(currentPort, rangeFromIncl, rangeToExcl);
      if (next == randomPort) {
        throw new PortRangeExhaustedException("No free ports in range " + rangeFromIncl + " (incl.) to " + rangeToExcl + " (excl.)");
      } else {
        currentPort = next;
      }
    }
  }
  
  /**
   *  Determines whether the local port is available for binding.
   *  
   *  @param port The port to check.
   *  @return True if the port is available for binding.
   */
  public static boolean isLocalPortAvailable(int port) {
    try {
      try (ServerSocket socket = new ServerSocket()) {
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(port));
      }
      return true;
    } catch (IOException e) {
      return false;
    }
  }
  
  static int randomInRange(int rangeFromIncl, int rangeToExcl) {
    return rangeFromIncl + (int) (Math.random() * (rangeToExcl - rangeFromIncl));
  }
  
  static int nextInRange(int current, int rangeFromIncl, int rangeToExcl) {
    final int range = rangeToExcl - rangeFromIncl;
    return rangeFromIncl + ((current - rangeFromIncl + 1) % range);
  }
  
  /**
   *  Determines whether the remote host is listening on the specified port. <p>
   *  
   *  This operation is blocking, optionally bounded by the specified timeout. Setting 
   *  {@code timeoutMillis} to zero will block indefinitely. <p>
   *  
   *  A timeout is equated to the remote port being closed. The rationale behind this
   *  decision is that a timeout condition (for a sensibly allocated timeout setting) 
   *  is often a result of either a firewall rule or an underlying network issue, and is,
   *  at any rate, not reachable from the client's perspective.
   *   
   *  @param host The host.
   *  @param port The port.
   *  @param timeoutMillis The timeout, or {@code 0} to
   *  @return True if the remote port is listening, or false if a connection couldn't be
   *          established (possibly due to a timeout).
   *  @throws IOException If an I/O error occurred.
   */
  public static boolean isRemotePortListening(String host, int port, int timeoutMillis) throws IOException {
    try (Socket s = new Socket()) {
      s.setReuseAddress(true);
      s.connect(new InetSocketAddress(host, port), timeoutMillis);
      return true;
    } catch (ConnectException | SocketTimeoutException e) {
      return false;
    }
  }
}
