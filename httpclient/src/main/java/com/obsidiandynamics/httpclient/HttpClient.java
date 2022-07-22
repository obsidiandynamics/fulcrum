package com.obsidiandynamics.httpclient;

import java.security.*;
import java.util.function.*;

import javax.net.ssl.*;

import org.apache.http.config.*;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.nio.client.*;
import org.apache.http.impl.nio.conn.*;
import org.apache.http.impl.nio.reactor.*;
import org.apache.http.nio.conn.*;
import org.apache.http.nio.conn.ssl.*;
import org.apache.http.nio.reactor.*;
import org.apache.http.ssl.SSLContexts;

import com.obsidiandynamics.func.*;

/**
 *  A builder for a {@link CloseableHttpAsyncClient} with configurable connection
 *  pooling and timeout behaviour.
 */
public final class HttpClient {
  private HttpClient() {}
  
  public static final class HttpClientBuilderException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    HttpClientBuilderException(Throwable cause) { super(cause); }
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  /**
   *  A builder for a {@link CloseableHttpAsyncClient}.
   */
  public static final class Builder {
    private int connectTimeout = 10_000;
    
    private int socketTimeout = 10_000;
    
    private int maxSelectInterval = 1_000;
  
    private int poolSize = 8;
    
    private HostnameVerifier hostnameVerifier = getDefaultHostnameVerifier();
    
    private SSLContext sslContext = getDefaultSSLContext();
    
    private Consumer<IOReactorConfig.Builder> customConfigurator = __ -> {};
    
    Builder() {}
    
    /**
     *  Sets both the socket and connect timeouts to a single specified value.
     *  
     *  @param timeoutMillis The timeout, in milliseconds.
     *  @return A {@link Builder} instance for chaining.
     */
    public Builder withTimeout(int timeoutMillis) {
      return withConnectTimeout(timeoutMillis).withSocketTimeout(timeoutMillis);
    }
    
    /**
     *  Sets the connect timeout.
     *  
     *  @param connectTimeoutMillis The timeout, in milliseconds. {@code 0} means no timeout.
     *  @return A {@link Builder} instance for chaining.
     */
    public Builder withConnectTimeout(int connectTimeoutMillis) {
      this.connectTimeout = connectTimeoutMillis;
      return this;
    }
    
    /**
     *  Sets the socket receive timeout.
     *  
     *  @param socketTimeoutMillis The timeout, in milliseconds. {@code 0} means no timeout.
     *  @return A {@link Builder} instance for chaining.
     */
    public Builder withSocketTimeout(int socketTimeoutMillis) {
      this.socketTimeout = socketTimeoutMillis;
      return this;
    }
    
    /**
     *  Sets the upper bound on the connection pool size.
     *  
     *  @param poolSize The connection pool size.
     *  @return A {@link Builder} instance for chaining.
     */
    public Builder withPoolSize(int poolSize) {
      this.poolSize = poolSize;
      return this;
    }
    
    /**
     *  Sets the upper bound on the interval at which the I/O reactor wakes up to check
     *  for timed out sessions and session requests. <p>
     *  
     *  The actual interval may be lower if he socket timeout is shorter than the max select interval.
     *  
     *  @param maxSelectIntervalMillis The maximum select interval, in milliseconds.
     *  @return A {@link Builder} instance for chaining.
     */
    public Builder withMaxSelectInterval(int maxSelectIntervalMillis) {
      this.maxSelectInterval = maxSelectIntervalMillis;
      return this;
    }
    
    /**
     *  Sets the hostname verifier. The default is given by {@link #getDefaultHostnameVerifier()}.
     *  
     *  @param hostnameVerifier The hostname verifier.
     *  @return A {@link Builder} instance for chaining.
     */
    public Builder withHostnameVerifier(HostnameVerifier hostnameVerifier) {
      this.hostnameVerifier = hostnameVerifier;
      return this;
    }
  
    /**
     *  Sets the SSL context. The default is given by {@link #getDefaultSSLContext()}.
     *  
     *  @param sslContext The SSL context.
     *  @return A {@link Builder} instance for chaining.
     */
    public Builder withSSLContext(SSLContext sslContext) {
      this.sslContext = sslContext;
      return this;
    }
    
    /**
     *  An extensibility point for custom manipulation of {@link IOReactorConfig}. <p>
     *  
     *  Use a custom configurator to set low-level socket options such as {@code SO_LINGER}, RX/TX
     *  buffer sizes, reuse, {@code SO_NODELAY}, etc., as well as the select interval and the number 
     *  of IO threads.
     *  
     *  @param customConfigurator A custom configuration.
     *  @return A {@link Builder} instance for chaining.
     */
    public Builder withCustomConfigurator(Consumer<IOReactorConfig.Builder> customConfigurator) {
      this.customConfigurator = customConfigurator;
      return this;
    }
  
    /**
     *  Builds a {@link CloseableHttpAsyncClient}.
     *  
     *  @return A new {@link CloseableHttpAsyncClient} instance.
     */
    public CloseableHttpAsyncClient build() {
      return Exceptions.wrap(this::buildChecked, HttpClientBuilderException::new);
    }
    
    /**
     *  Builds a {@link CloseableHttpAsyncClient} and starts the instance prior to returning it.
     *  
     *  @return A started {@link CloseableHttpAsyncClient} instance.
     */
    public CloseableHttpAsyncClient buildAndStart() {
      final CloseableHttpAsyncClient client = build();
      client.start();
      return client;
    }
    
    private CloseableHttpAsyncClient buildChecked() throws IOReactorException {
      final Registry<SchemeIOSessionStrategy> sessionStrategy = RegistryBuilder
          .<SchemeIOSessionStrategy>create()
          .register("http", NoopIOSessionStrategy.INSTANCE)
          .register("https", new SSLIOSessionStrategy(sslContext, hostnameVerifier))
          .build();
  
      final int selectInterval = Math.max(1, Math.min(maxSelectInterval, socketTimeout));
      final org.apache.http.impl.nio.reactor.IOReactorConfig.Builder reactorConfigBuilder = IOReactorConfig.custom()
          .setSelectInterval(selectInterval)
          .setSoTimeout(socketTimeout)
          .setConnectTimeout(connectTimeout);
      customConfigurator.accept(reactorConfigBuilder);
      final DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(reactorConfigBuilder.build());
      final PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioReactor, sessionStrategy);
      connectionManager.setMaxTotal(Integer.MAX_VALUE);
      connectionManager.setDefaultMaxPerRoute(poolSize);
  
      return HttpAsyncClients.custom()
          .setConnectionManager(connectionManager)
          .build();
    }
  }

  public static HostnameVerifier getDefaultHostnameVerifier() {
    return new DefaultHostnameVerifier();
  }
  
  public static HostnameVerifier getTrustingHostnameVerifier() {
    return NoopHostnameVerifier.INSTANCE;
  }
  
  public static SSLContext getDefaultSSLContext() {
    return Exceptions.wrap(SSLContext::getDefault, HttpClientBuilderException::new);
  }
  
  public static SSLContext getTrustingSSLContext() {
    return Exceptions.wrap(HttpClient::getSSLContextChecked, HttpClientBuilderException::new);
  }

  private static SSLContext getSSLContextChecked() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
    return SSLContexts.custom().loadTrustMaterial(null, (__chain, __authType) -> true).build();
  }
}
