package com.obsidiandynamics.httpclient;

import java.net.*;

import org.apache.http.client.utils.*;

import com.obsidiandynamics.func.*;

/**
 *  A fluent wrapper around {@link org.apache.http.client.utils.URIBuilder}. The same basic
 *  behaviour as the conventional {@link URIBuilder} is exposed, but in a way that promotes
 *  method chaining.
 */
public final class FluentURI {
  private final URIBuilder builder;

  public FluentURI() {
    builder = new URIBuilder();
  }

  public FluentURI(String initialFormat, Object... initialArgs) {
    this(String.format(initialFormat, initialArgs));
  }

  public FluentURI(String initialUri) {
    builder = Exceptions.wrap(() -> new URIBuilder(initialUri), InvalidURISyntaxException::new);
  }

  public FluentURI withScheme(String scheme) {
    builder.setScheme(scheme);
    return this;
  }

  public FluentURI withUserInfo(String userInfo) {
    builder.setUserInfo(userInfo);
    return this;
  }

  public FluentURI withUserInfo(String username, String password) {
    return withUserInfo(username + ':' + password);
  }

  public FluentURI withHost(String host) {
    builder.setHost(host);
    return this;
  }

  public FluentURI withPort(int port) {
    builder.setPort(port);
    return this;
  }

  public FluentURI withPath(String path) {
    builder.setPath(path);
    return this;
  }
  
  /**
   *  Adds a parameter with no value.
   *  
   *  @param key The parameter key.
   *  @return This {@link FluentURI} for chaining.
   */
  public FluentURI withParameter(String key) {
    builder.addParameter(key, null);
    return this;
  }
  
  /**
   *  Adds a query parameter if and only if the supplied {@code value} is not null. The value will be
   *  represented using its {@link #toString()} method. <p>
   *  
   *  If you need to add a parameter with no value, use the single-argument {@link #withParameter(String)}
   *  method instead.
   *  
   *  @param key The parameter key.
   *  @param value The parameter value.
   *  @return This {@link FluentURI} for chaining.
   */
  public FluentURI withParameter(String key, Object value) {
    if (value != null) {
      builder.addParameter(key, value.toString());
    }
    return this;
  }
  
  public FluentURI withFragment(String fragment) {
    builder.setFragment(fragment);
    return this;
  }

  public URI build() {
    return Exceptions.wrap(() -> builder.build(), InvalidURISyntaxException::new);
  }
}
