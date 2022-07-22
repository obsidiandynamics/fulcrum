package com.obsidiandynamics.httpclient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.nio.client.*;
import org.apache.http.util.*;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.junit.runners.*;

import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.junit.*;
import com.obsidiandynamics.assertion.*;
import com.obsidiandynamics.httpclient.HttpClient.*;
import com.obsidiandynamics.io.*;
import com.obsidiandynamics.junit.*;

@RunWith(Parameterized.class)
public final class HttpClientTest {
  @Parameterized.Parameters
  public static List<Object[]> data() {
    return TestCycle.timesQuietly(1);
  }
  
  private CloseableHttpAsyncClient client;
  
  @Rule
  public final ExpectedException expectedException = ExpectedException.none();
  
  @ClassRule
  public static final WireMockClassRule wireMockClassRule = new WireMockClassRule(options()
                                                                                        .dynamicPort()
                                                                                        .dynamicHttpsPort());
  @Rule
  public final WireMockClassRule wireMock = wireMockClassRule;
  
  @After
  public void after() {
    if (client != null) {
      IO.closeUnchecked(client);
    }
  }
  
  @Test
  public void testConformance() {
    Assertions.assertUtilityClassWellDefined(HttpClient.class);
  }
  
  @Test(expected=RuntimeIOException.class)
  public void testCloseUncheckedWithException() {
    final Closeable closeable = () -> {
      throw new IOException("Simulated fault");
    };
    IO.closeUnchecked(closeable);
  }
  
  @Test
  public void testGetHttp() throws InterruptedException, ExecutionException, IOException {
    client = HttpClient.builder()
        .withMaxSelectInterval(1)
        .withCustomConfigurator(configBuilder -> {
          configBuilder
          .setIoThreadCount(1)
          .setSoLinger(0);
        })
        .buildAndStart();

    wireMock.stubFor(get(urlEqualTo("/test"))
                     .withHeader("Accept", equalTo("application/json"))
                     .willReturn(aResponse()
                                 .withStatus(200)
                                 .withHeader("Content-Type", "application/json")
                                 .withBody("{}")));
    
    client.start();
    final String url = String.format("http://localhost:%d/test", wireMock.port());
    final HttpGet get = new HttpGet(url);
    get.addHeader("Accept", "application/json");
    final Future<HttpResponse> future = client.execute(get, null);
    final HttpResponse response = future.get();
    final String responseEntity = EntityUtils.toString(response.getEntity());
    assertEquals("{}", responseEntity);
  }
  
  @Test
  public void testGetHttps() throws InterruptedException, ExecutionException, IOException {
    client = HttpClient.builder()
        .withHostnameVerifier(HttpClient.getTrustingHostnameVerifier())
        .withSSLContext(HttpClient.getTrustingSSLContext())
        .buildAndStart();
    
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .withHeader("Accept", equalTo("application/json"))
                     .willReturn(aResponse()
                                 .withStatus(200)
                                 .withHeader("Content-Type", "application/json")
                                 .withBody("{}")));
    
    final String url = String.format("https://localhost:%d/test", wireMock.httpsPort());
    final HttpGet get = new HttpGet(url);
    get.addHeader("Accept", "application/json");
    final Future<HttpResponse> future = client.execute(get, null);
    final HttpResponse response = future.get();
    final String responseEntity = EntityUtils.toString(response.getEntity());
    assertEquals("{}", responseEntity);
  }
  
  @Test(expected=HttpClientBuilderException.class)
  public void testRuntimeException() throws IOException {
    HttpClient.builder().withPoolSize(-1).buildAndStart().close();
  }
  
  @Test
  public void testSocketTimeout() throws InterruptedException, ExecutionException {
    client = HttpClient.builder()
        .withSocketTimeout(1)
        .buildAndStart();
    
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .withHeader("Accept", equalTo("application/json"))
                     .willReturn(aResponse()
                                 .withFixedDelay(10)
                                 .withStatus(200)
                                 .withHeader("Content-Type", "application/json")
                                 .withBody("{}")));

    expectedException.expect(ExecutionException.class);
    expectedException.expectCause(isA(SocketTimeoutException.class));
    
    final String url = String.format("http://localhost:%d/test", wireMock.port());
    final HttpGet get = new HttpGet(url);
    get.addHeader("Accept", "application/json");
    
    // With small response delays, it's possible to get a late response without trapping a timeout, so we repeat the test until
    // we get the timeout exception. Increasing the response delays isn't a good idea with WireMock, as this causes the unit
    // test to run longer (waiting for the WireMock rule to clean up).
    for (;;) {
      final Future<HttpResponse> future = client.execute(get, null);
      future.get();
    }
  }
  
  @Test
  public void testNoTimeouts() throws InterruptedException, ExecutionException {
    client = HttpClient.builder()
        .withTimeout(0)
        .buildAndStart();
    
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .withHeader("Accept", equalTo("application/json"))
                     .willReturn(aResponse()
                                 .withFixedDelay(10)
                                 .withStatus(200)
                                 .withHeader("Content-Type", "application/json")
                                 .withBody("{}")));
    
    final String url = String.format("http://localhost:%d/test", wireMock.port());
    final HttpGet get = new HttpGet(url);
    get.addHeader("Accept", "application/json");
    
    final Future<HttpResponse> future = client.execute(get, null);
    future.get();
  }
  
  @Test
  public void testFault() throws InterruptedException, ExecutionException {
    client = HttpClient.builder().buildAndStart();
    
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .withHeader("Accept", equalTo("application/json"))
                     .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));
    
    client.start();
    expectedException.expect(ExecutionException.class);
    expectedException.expectCause(isA(MalformedChunkCodingException.class));
    
    final String url = String.format("http://localhost:%d/test", wireMock.port());
    final HttpGet get = new HttpGet(url);
    get.addHeader("Accept", "application/json");
    
    final Future<HttpResponse> future = client.execute(get, null);
    future.get();
  }
}
