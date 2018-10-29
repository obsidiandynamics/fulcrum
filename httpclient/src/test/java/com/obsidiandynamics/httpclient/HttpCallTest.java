package com.obsidiandynamics.httpclient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.*;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.impl.nio.client.*;
import org.hamcrest.core.*;
import org.junit.*;
import org.junit.rules.*;
import org.mockito.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.exc.*;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.junit.*;
import com.obsidiandynamics.func.*;
import com.obsidiandynamics.json.*;
import com.obsidiandynamics.junit.*;

public final class HttpCallTest {
  private static final class TestObject {
    @JsonProperty
    double value;
    
    TestObject(@JsonProperty("value") double value) { this.value = value; }
  }
  
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
  
  private static String toJson(Object obj) {
    return Json.getInstance().stringifyUnchecked(obj);
  }
  
  @Test
  public void testParse() throws JsonInputException, IOException, ResponseStatusException, ServiceInvocationException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .withHeader("Accept", equalTo("application/json"))
                     .willReturn(aResponse()
                                 .withStatus(200)
                                 .withHeader("Content-Type", "application/json")
                                 .withBody(toJson(new TestObject(3.14)))));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    get.addHeader("Accept", "application/json");
    final TestObject res = HttpCall.withClient(client).invoke(get).withJson(Json.getInstance()).parse(TestObject.class);
    assertEquals(3.14, res.value, Long.MIN_VALUE);
  }
  
  @Test
  public void testParseParametrized() throws JsonInputException, IOException, ResponseStatusException, ServiceInvocationException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .withHeader("Accept", equalTo("application/json"))
                     .willReturn(aResponse()
                                 .withStatus(201)
                                 .withHeader("Content-Type", "application/json")
                                 .withBody(toJson(Collections.singleton(new TestObject(3.14))))));
    
    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    get.addHeader("Accept", "application/json");
    final List<TestObject> res = Classes.<List<TestObject>>cast(HttpCall.withClient(client).invoke(get)
                                                                .parse(List.class, TestObject.class));
    assertEquals(1, res.size());
    assertEquals(3.14, res.get(0).value, Long.MIN_VALUE);
  }
  
  @Test
  public void testParseWrongType() throws JsonInputException, IOException, ResponseStatusException, ServiceInvocationException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .withHeader("Accept", equalTo("application/json"))
                     .willReturn(aResponse()
                                 .withStatus(200)
                                 .withHeader("Content-Type", "application/json")
                                 .withBody("{\"value\":\"foo\"}")));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    get.addHeader("Accept", "application/json");
    
    expectedException.expect(JsonInputException.class);
    expectedException.expectCause(IsInstanceOf.instanceOf(InvalidFormatException.class));
    HttpCall.withClient(client).invoke(get).parse(TestObject.class);
  }
  
  @Test
  public void testParseWrongStatus() throws JsonInputException, IOException, ResponseStatusException, ServiceInvocationException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .withHeader("Accept", equalTo("application/json"))
                     .willReturn(aResponse()
                                 .withStatus(400)
                                 .withHeader("Content-Type", "application/json")
                                 .withBody("{}")));
    
    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    get.addHeader("Accept", "application/json");

    expectedException.expect(ResponseStatusException.class);
    expectedException.expectCause(IsNull.nullValue(Throwable.class));
    HttpCall.withClient(client).invoke(get).parse(TestObject.class);
  }
  
  static final class CustomError {
    @JsonProperty
    final String status;
    
    CustomError(@JsonProperty("status") String status) { this.status = status; }
  }

  @Test
  public void testParseWithCustomError() throws JsonInputException, IOException, ResponseStatusException, ServiceInvocationException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .withHeader("Accept", equalTo("application/json"))
                     .willReturn(aResponse()
                                 .withStatus(400)
                                 .withHeader("Content-Type", "application/json")
                                 .withBody(toJson(new CustomError("SOME_ERROR")))));
    
    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    get.addHeader("Accept", "application/json");

    expectedException.expect(ResponseStatusException.class);
    expectedException.expect(HamcrestMatchers.<ResponseStatusException>assertedBy(e -> {
      assertNotNull(e.getError());
      assertEquals("SOME_ERROR", e.<CustomError>getError().status);
    }));
    expectedException.expectCause(IsNull.nullValue(Throwable.class));
    HttpCall.withClient(client).invoke(get).withErrorType(CustomError.class).parse(TestObject.class);
  }

  @Test
  public void testParseWithFaultConnectionReset() throws JsonInputException, IOException, ResponseStatusException, ServiceInvocationException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
    
    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));

    expectedException.expect(ServiceInvocationException.class);
    expectedException.expectCause(IsInstanceOf.instanceOf(IOException.class));
    HttpCall.withClient(client).invoke(get);
  }
  
  @Test
  public void testParseWithFaultEmptyResponse() throws JsonInputException, IOException, ResponseStatusException, ServiceInvocationException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));
    
    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));

    expectedException.expect(ServiceInvocationException.class);
    expectedException.expectCause(IsInstanceOf.instanceOf(ConnectionClosedException.class));
    HttpCall.withClient(client).invoke(get);
  }
  
  @Test
  public void testParseWithFaultMalformedResponse() throws JsonInputException, IOException, ResponseStatusException, ServiceInvocationException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));
    
    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));

    expectedException.expect(ServiceInvocationException.class);
    expectedException.expectCause(IsInstanceOf.instanceOf(MalformedChunkCodingException.class));
    HttpCall.withClient(client).invoke(get);
  }
  
  @Test
  public void testParseIfFound() throws JsonInputException, IOException, ResponseStatusException, ServiceInvocationException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .withHeader("Accept", equalTo("application/json"))
                     .willReturn(aResponse()
                                 .withStatus(200)
                                 .withHeader("Content-Type", "application/json")
                                 .withBody(toJson(new TestObject(3.14)))));
    
    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    get.addHeader("Accept", "application/json");
    final TestObject res = HttpCall.withClient(client).invoke(get).parseIfFound(TestObject.class);
    assertEquals(3.14, res.value, Long.MIN_VALUE);
  }
  
  @Test
  public void testParseIfFound404() throws JsonInputException, IOException, ResponseStatusException, ServiceInvocationException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .withHeader("Accept", equalTo("application/json"))
                     .willReturn(aResponse()
                                 .withStatus(404)));
    
    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    get.addHeader("Accept", "application/json");
    final TestObject res = HttpCall.withClient(client).invoke(get).parseIfFound(TestObject.class);
    assertNull(res);
  }
  
  @Test
  public void testParseIfFoundParametrized() throws JsonInputException, IOException, ResponseStatusException, ServiceInvocationException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .withHeader("Accept", equalTo("application/json"))
                     .willReturn(aResponse()
                                 .withStatus(201)
                                 .withHeader("Content-Type", "application/json")
                                 .withBody(toJson(Collections.singleton(new TestObject(3.14))))));
    
    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    get.addHeader("Accept", "application/json");
    final List<TestObject> res = Classes.<List<TestObject>>cast(HttpCall.withClient(client).invoke(get)
                                                                .parseIfFound(List.class, TestObject.class));
    assertEquals(1, res.size());
    assertEquals(3.14, res.get(0).value, Long.MIN_VALUE);
  }
  
  @Test
  public void testParseIfFoundParametrized404() throws JsonInputException, IOException, ResponseStatusException, ServiceInvocationException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .withHeader("Accept", equalTo("application/json"))
                     .willReturn(aResponse()
                                 .withStatus(404)));
    
    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    get.addHeader("Accept", "application/json");
    final List<TestObject> res = Classes.<List<TestObject>>cast(HttpCall.withClient(client).invoke(get)
                                                                .parseIfFound(List.class, TestObject.class));
    assertNull(res);
  }
  
  @Test
  public void testEnsureResponseStatus() throws JsonInputException, IOException, ResponseStatusException, ServiceInvocationException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(200)));
    
    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    HttpCall.withClient(client).invoke(get).ensureStatus(200);
  }
  
  @Test
  public void testEnsureResponseStatus400() throws JsonInputException, IOException, ResponseStatusException, ServiceInvocationException, InterruptedException {
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
    }));
    HttpCall.withClient(client).invoke(get).ensureStatus(200);
  }
  
  @Test
  public void testEnsureResponseStatusNoEntity() throws JsonInputException, IOException, ResponseStatusException, ServiceInvocationException, InterruptedException {
    wireMock.stubFor(head(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(400)));

    final HttpHead head = new HttpHead(String.format("http://localhost:%d/test", wireMock.port()));
    
    expectedException.expect(ResponseStatusException.class);
    expectedException.expectCause(IsNull.nullValue(Throwable.class));
    HttpCall.withClient(client).invoke(head).ensureStatus(200);
  }
  
  @Test
  public void testEnsureResponseStatusBadJson() throws JsonInputException, IOException, ResponseStatusException, ServiceInvocationException, InterruptedException {
    wireMock.stubFor(get(urlEqualTo("/test"))
                     .willReturn(aResponse()
                                 .withStatus(400)
                                 .withHeader("Content-Type", "application/json")
                                 .withBody("}{ not valid json")));

    final HttpGet get = new HttpGet(String.format("http://localhost:%d/test", wireMock.port()));
    
    try {
      HttpCall.withClient(client).invoke(get).ensureStatus(200);
      fail();
    } catch (ResponseStatusException e) {
      assertEquals("}{ not valid json", e.getError());
    }
  }
  
  @Test
  public void testParseEntity() throws UnsupportedEncodingException, IOException {
    final String entityString = HttpCall.parseEntity(new StringEntity("stringEntity"));
    assertEquals("stringEntity", entityString);
  }
  
  @Test
  public void testParseEntityWithParseError() throws IOException {
    expectedException.expect(IOException.class);
    expectedException.expectCause(Is.isA(ParseException.class));
    final HttpEntity entity = mock(HttpEntity.class, Answers.CALLS_REAL_METHODS);
    final Header header = mock(Header.class);
    when(entity.getContentType()).thenReturn(header);
    when(header.getElements()).thenThrow(ParseException.class);
    HttpCall.parseEntity(entity);
  }
}
