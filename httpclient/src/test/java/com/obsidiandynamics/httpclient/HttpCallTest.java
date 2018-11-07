package com.obsidiandynamics.httpclient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.*;

import org.apache.http.*;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.impl.nio.client.*;
import org.assertj.core.api.*;
import org.hamcrest.core.*;
import org.junit.*;
import org.junit.rules.*;
import org.mockito.*;

import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.junit.*;
import com.obsidiandynamics.httpclient.HttpCall.*;
import com.obsidiandynamics.junit.*;

public final class HttpCallTest {
  private static CloseableHttpAsyncClient client;

  @ClassRule
  public static WireMockClassRule wireMockClassRule = new WireMockClassRule(options()
                                                                            .dynamicPort()
                                                                            .dynamicHttpsPort());
  @Rule
  public final WireMockClassRule wireMock = wireMockClassRule;

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

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

    expectedException.expect(NumberFormatException.class);
    HttpCall.withClient(client).invoke(get).parse(Double::parseDouble);
  }

  @Test
  public void testParseWrongStatusWithEntity() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(400)
                                 .withStatusMessage("Bad request")
                                 .withBody("{some entity}")));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));

    expectedException.expect(ResponseStatusException.class);
    expectedException.expect(HamcrestMatchers.<ResponseStatusException>assertedBy(exception -> {
      assertEquals(400, exception.getStatusCode());
      assertEquals("Bad request", exception.getReasonPhrase());
      assertEquals("{some entity}", exception.getEntity());
      Assertions.assertThat(exception.getMessage()).contains("Unexpected response");
      Assertions.assertThat(exception.getMessage()).contains("status: 400");
      Assertions.assertThat(exception.getMessage()).contains("reason: Bad request");
      Assertions.assertThat(exception.getMessage()).contains("entity: '{some entity}'");
    }));
    HttpCall.withClient(client).invoke(get).ensureIsOk();
  }

  @Test
  public void testParseWrongStatusWithoutEntity() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(head(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(400)
                                 .withStatusMessage("Bad request")));

    final HttpHead head = new HttpHead(String.format("http://localhost:%d/test", wireMock.port()));

    expectedException.expect(ResponseStatusException.class);
    expectedException.expect(HamcrestMatchers.<ResponseStatusException>assertedBy(exception -> {
      assertEquals(400, exception.getStatusCode());
      assertEquals("Bad request", exception.getReasonPhrase());
      assertNull(exception.getEntity());
      Assertions.assertThat(exception.getMessage()).contains("Unexpected response");
      Assertions.assertThat(exception.getMessage()).contains("status: 400");
      Assertions.assertThat(exception.getMessage()).contains("reason: Bad request");
      Assertions.assertThat(exception.getMessage()).contains("entity: null");
    }));
    HttpCall.withClient(client).invoke(head).ensureIsOk();
  }

  @Test
  public void testParseWithFaultConnectionReset() throws IOException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));

    expectedException.expect(IOException.class);
    HttpCall.withClient(client).invoke(get);
  }

  @Test
  public void testParseWithFaultEmptyResponse() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));

    expectedException.expect(ConnectionClosedException.class);
    HttpCall.withClient(client).invoke(get);
  }

  @Test
  public void testParseWithFaultMalformedResponse() throws IOException, ResponseStatusException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));

    expectedException.expect(MalformedChunkCodingException.class);
    HttpCall.withClient(client).invoke(get);
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

    expectedException.expect(ResponseStatusException.class);
    expectedException.expectCause(IsNull.nullValue(Throwable.class));
    expectedException.expect(HamcrestMatchers.<ResponseStatusException>assertedBy(exception -> {
      assertEquals(400, exception.getStatusCode());
      assertEquals("Bad request", exception.getReasonPhrase());
      assertEquals("", exception.getEntity());
    }));
    HttpCall.withClient(client).invoke(get).ensureIsOk();
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

    expectedException.expect(ResponseStatusException.class);
    expectedException.expectCause(IsNull.nullValue(Throwable.class));
    expectedException.expect(HamcrestMatchers.<ResponseStatusException>assertedBy(exception -> {
      assertEquals(400, exception.getStatusCode());
      assertEquals("Bad request", exception.getReasonPhrase());
      assertEquals("", exception.getEntity());
    }));
    HttpCall.withClient(client).invoke(get).ensureIsOkOrCreated();
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
  public void testEntityToString() throws UnsupportedEncodingException, IOException {
    final String entityString = HttpCall.toString(new StringEntity("stringEntity"));
    assertEquals("stringEntity", entityString);
  }

  @Test
  public void testEntityToStringWithParseError() throws IOException {
    final HttpEntity entity = mock(HttpEntity.class, Answers.CALLS_REAL_METHODS);
    final Header header = mock(Header.class);
    when(entity.getContentType()).thenReturn(header);
    when(header.getElements()).thenThrow(ParseException.class);

    expectedException.expect(IOException.class);
    expectedException.expectCause(Is.isA(ParseException.class));
    HttpCall.toString(entity);
  }
}
