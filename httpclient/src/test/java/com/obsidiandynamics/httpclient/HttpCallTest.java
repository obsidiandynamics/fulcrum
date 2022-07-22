package com.obsidiandynamics.httpclient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.obsidiandynamics.func.Functions.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.http.*;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.impl.nio.client.*;
import org.assertj.core.api.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.mockito.*;

import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.junit.*;
import com.obsidiandynamics.httpclient.HttpCall.*;
import com.obsidiandynamics.junit.*;

@RunWith(Parameterized.class)
public final class HttpCallTest {
  @Parameterized.Parameters
  public static List<Object[]> data() {
    return TestCycle.timesQuietly(1);
  }
  
  private static CloseableHttpAsyncClient client;

  @ClassRule
  public static final WireMockClassRule wireMockClassRule = new WireMockClassRule(options()
                                                                                        .dynamicPort()
                                                                                        .dynamicHttpsPort());
  @Rule
  public final WireMockClassRule wireMock = wireMockClassRule;

  @BeforeClass
  public static void beforeClass() {
    client = HttpClient.builder().buildAndStart();
  }

  @AfterClass
  public static void afterClass() throws IOException {
    if (client != null) {
      client.close();
      client = null;
    }
  }

  @Test
  public void testGet() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(200)
                                 .withStatusMessage("OK")
                                 .withBody("3.14")));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    final HttpCallResponse res = HttpCall.withClient(client).invoke(get);
    assertEquals(200, res.getStatusCode());
    assertEquals("OK", res.getReasonPhrase());
    assertEquals("3.14", res.getEntityString());
    assertNotNull(res.getResponse());
  }

  @Test
  public void testParse() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(200)
                                 .withBody("3.14")));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    final double res = HttpCall.withClient(client).invoke(get).parse(Double::parseDouble);
    assertEquals(3.14, res, Double.MIN_VALUE);
  }

  @Test
  public void testParseWrongType() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(200)
                                 .withBody("gibberish")));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));

    Assertions.assertThatThrownBy(() -> {
      HttpCall.withClient(client).invoke(get).parse(Double::parseDouble);
    }).isExactlyInstanceOf(NumberFormatException.class);
  }

  @Test
  public void testParseWrongStatusWithEntity() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(400)
                                 .withStatusMessage("Bad request")
                                 .withBody("{some entity}")));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));

    Assertions.assertThatThrownBy(() -> {
      HttpCall.withClient(client).invoke(get).ensureIsOk();
    }).satisfies(throwable -> {
      final ResponseStatusException rse = mustBeSubtype(throwable, ResponseStatusException.class, AssertionError::new);
      assertEquals(400, rse.getStatusCode());
      assertEquals("Bad request", rse.getReasonPhrase());
      assertEquals("{some entity}", rse.getEntity());
      Assertions.assertThat(rse.getMessage()).contains("Unexpected response");
      Assertions.assertThat(rse.getMessage()).contains("status: 400");
      Assertions.assertThat(rse.getMessage()).contains("reason: Bad request");
      Assertions.assertThat(rse.getMessage()).contains("entity: '{some entity}'");
    });
  }

  @Test
  public void testParseWrongStatusWithoutEntity() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(head(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(400)
                                 .withStatusMessage("Bad request")));

    final HttpHead head = new HttpHead(String.format("http://localhost:%d/test", wireMock.port()));

    Assertions.assertThatThrownBy(() -> {
      HttpCall.withClient(client).invoke(head).ensureIsOk();
    }).satisfies(throwable -> {
      final ResponseStatusException rse = mustBeSubtype(throwable, ResponseStatusException.class, AssertionError::new);
      assertEquals(400, rse.getStatusCode());
      assertEquals("Bad request", rse.getReasonPhrase());
      assertNull(rse.getEntity());
      Assertions.assertThat(rse.getMessage()).contains("Unexpected response");
      Assertions.assertThat(rse.getMessage()).contains("status: 400");
      Assertions.assertThat(rse.getMessage()).contains("reason: Bad request");
      Assertions.assertThat(rse.getMessage()).contains("entity: null");
    });
  }

  @Test
  public void testParseWithFaultConnectionReset() throws IOException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));

    Assertions.assertThatThrownBy(() -> {
      HttpCall.withClient(client).invoke(get);
    }).isInstanceOf(IOException.class);
  }

  @Test
  public void testParseWithFaultEmptyResponse() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));

    Assertions.assertThatThrownBy(() -> {
      HttpCall.withClient(client).invoke(get);
    }).isExactlyInstanceOf(ConnectionClosedException.class);
  }

  @Test
  public void testParseWithFaultMalformedResponse() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));

    Assertions.assertThatThrownBy(() -> {
      HttpCall.withClient(client).invoke(get);
    }).isExactlyInstanceOf(MalformedChunkCodingException.class);
  }

  @Test
  public void testParseIfOkWithOkResponse() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(200)
                                 .withBody("3.14")));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    final double res = HttpCall.withClient(client).invoke(get).ifOk().parse(Double::parseDouble);
    assertEquals(3.14, res, Double.MIN_VALUE);
  }

  @Test
  public void testParseIfOkOrCreatedWithOkResponse() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(200)
                                 .withBody("3.14")));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    final double res = HttpCall.withClient(client).invoke(get).ifStatusIs(HttpStatus.SC_OK, HttpStatus.SC_CREATED).parse(Double::parseDouble);
    assertEquals(3.14, res, Double.MIN_VALUE);
  }

  @Test
  public void testParseIfOkWithNotFoundResponse() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(404)));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    final Double res = HttpCall.withClient(client).invoke(get).ensureIsOkOrNotFound().ifOk().parse(Double::parseDouble);
    assertNull(res);
  }

  @Test
  public void testEnsureIsOkWithOkResponse() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(200)));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    HttpCall.withClient(client).invoke(get).ensureIsOk();
  }

  @Test
  public void testEnsureIsOkWithBadRequest() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(400)
                                 .withStatusMessage("Bad request")));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));

    Assertions.assertThatThrownBy(() -> {
      HttpCall.withClient(client).invoke(get).ensureIsOk();
    }).satisfies(throwable -> {
      final ResponseStatusException rse = mustBeSubtype(throwable, ResponseStatusException.class, AssertionError::new);
      assertNull(rse.getCause());
      assertEquals(400, rse.getStatusCode());
      assertEquals("Bad request", rse.getReasonPhrase());
      assertEquals("", rse.getEntity());
    });
  }

  @Test
  public void testEnsureIsOkOrCreatedWithOkResponse() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(200)));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    HttpCall.withClient(client).invoke(get).ensureIsOkOrCreated();
  }

  @Test
  public void testEnsureIsOkOrCreatedWithBadRequest() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(400)
                                 .withStatusMessage("Bad request")));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));

    Assertions.assertThatThrownBy(() -> {
      HttpCall.withClient(client).invoke(get).ensureIsOkOrCreated();
    }).satisfies(throwable -> {
      final ResponseStatusException rse = mustBeSubtype(throwable, ResponseStatusException.class, AssertionError::new);
      assertNull(rse.getCause());
      assertEquals(400, rse.getStatusCode());
      assertEquals("Bad request", rse.getReasonPhrase());
      assertEquals("", rse.getEntity());
    });
  }

  @Test
  public void testEnsureIsCreatedWithCreatedResponse() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(201)));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    HttpCall.withClient(client).invoke(get).ensureIsCreated();
  }

  @Test
  public void testEnsureIsNoContentWithNoContentResponse() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(204)));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    HttpCall.withClient(client).invoke(get).ensureIsNoContent();
  }

  @Test
  public void testEntityToString() throws IOException {
    final String entityString = HttpCall.toString(new StringEntity("stringEntity"));
    assertEquals("stringEntity", entityString);
  }

  @Test
  public void testEntityToStringWithParseError() throws IOException {
    final HttpEntity entity = mock(HttpEntity.class, Answers.CALLS_REAL_METHODS);
    final Header header = mock(Header.class);
    when(entity.getContentType()).thenReturn(header);
    when(header.getElements()).thenThrow(ParseException.class);

    Assertions.assertThatThrownBy(() -> {
      HttpCall.toString(entity);
    }).isExactlyInstanceOf(IOException.class).hasCauseExactlyInstanceOf(ParseException.class);
  }
  
  @Test
  public void testCoerceToIOException_ioException() {
    final IOException cause = new IOException();
    final IOException coerced = HttpCall.coerceToIOException(new ExecutionException(cause));
    assertSame(cause, coerced);
  }
  
  @Test
  public void testCoerceToIOException_otherException() {
    final Exception cause = new Exception();
    final IOException coerced = HttpCall.coerceToIOException(new ExecutionException(cause));
    Assertions.assertThat(coerced).isExactlyInstanceOf(IOException.class).hasCauseReference(cause);
  }
}
