package io.tesmon;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.ConnectException;
import java.util.UUID;
import java.util.concurrent.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.*;

public class TesmonEventClientTests {

  private static final String BASE_URL = "http://http://localhost:8080";
  private static final String EVENT_KEY = "testEvent";
  private static final JSONObject EVENT_BODY = new JSONObject().put("key", "value");

  private TesmonEventClient tesmonEventClient;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void sendEvent_successful() throws ExecutionException, InterruptedException, TimeoutException {
    tesmonEventClient = new DefaultTesmonEventClient("1001", "api-token", "http://localhost:8080");
    String expectedEventId = UUID.randomUUID().toString();
    JSONObject expected = new JSONObject();
    expected.put("eventId", expectedEventId);

    // Start WireMock server
    WireMockServer wireMockServer = new WireMockServer(options().port(8080));
    wireMockServer.start();

    // Configure WireMock to return a mock response from localhost
    configureFor("localhost", 8080);
    stubFor(post(urlEqualTo("/v1/environments/1001/events"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", "application/json")
            .withBody("{ \"eventId\": \"" + expectedEventId + "\" }")));

    Future<String> future = tesmonEventClient.sendEvent(EVENT_KEY, EVENT_BODY);
    String actual = future.get(5, TimeUnit.SECONDS);
    JSONObject actualJson = new JSONObject(actual);
    assertEquals(expected.toString(), actualJson.toString());

    // Stop WireMock server
    wireMockServer.stop();
  }

  @Test
  void sendEvent_badRequest() throws ExecutionException, InterruptedException {
    tesmonEventClient = new DefaultTesmonEventClient("1001", "api-token", "http://localhost:8080");
    // Start WireMock server
    WireMockServer wireMockServer = new WireMockServer(options().port(8080));
    wireMockServer.start();

    // Configure WireMock to return a bad request response from localhost
    configureFor("localhost", 8080);
    stubFor(post(urlEqualTo("/v1/environments/1001/events"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_BAD_REQUEST)
            .withHeader("Content-Type", "application/json")
            .withBody("{ \"message\": \"Invalid request body\" }")));

    CompletableFuture<String> future = tesmonEventClient.sendEvent(EVENT_KEY, EVENT_BODY);
    String actual = future.get();
    JSONObject actualJson = new JSONObject(actual);
    assertEquals(new JSONObject("{\"message\": \"Invalid request body\"}").toString(), actualJson.toString());

    // Stop WireMock server
    wireMockServer.stop();
  }

  @Test
  void sendEvent_internalServerError() throws ExecutionException, InterruptedException {
    tesmonEventClient = new DefaultTesmonEventClient("1001", "api-token", "http://localhost:8080");
    // Start WireMock server
    WireMockServer wireMockServer = new WireMockServer(options().port(8080));
    wireMockServer.start();

    // Configure WireMock to return an internal server error response from localhost
    configureFor("localhost", 8080);
    stubFor(post(urlEqualTo("/v1/environments/1001/events"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)
            .withHeader("Content-Type", "application/json")
            .withBody("{ \"message\": \"Internal server error\" }")));

    Future<String> future = tesmonEventClient.sendEvent(EVENT_KEY, EVENT_BODY);
    String actual = future.get();
    JSONObject actualJson = new JSONObject(actual);

    assertEquals(new JSONObject("{\"message\": \"Internal server error\"}").toString(), actualJson.toString());

    // Stop WireMock server
    wireMockServer.stop();
  }

  @Test
  void sendEvent_unexpectedResponse() throws IOException, ExecutionException, InterruptedException {
    tesmonEventClient = new DefaultTesmonEventClient("1001", "api-token", "http://localhost:8080");
    // Start WireMock server
    WireMockServer wireMockServer = new WireMockServer(options().port(8080));
    wireMockServer.start();

    // Configure WireMock to return an unexpected response from localhost
    configureFor("localhost", 8080);
    stubFor(post(urlEqualTo("/v1/environments/1001/events"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_BAD_REQUEST)
            .withHeader("Content-Type", "application/xml")
            .withBody("{ \"message\": \"Unexpected response\" }")));

    Future<String> future = tesmonEventClient.sendEvent(EVENT_KEY, EVENT_BODY);
    String actual = future.get();
    JSONObject actualJson = new JSONObject(actual);

    assertEquals(new JSONObject("{\"message\": \"Unexpected response\"}").toString(), actualJson.toString());

    // Stop WireMock server
    wireMockServer.stop();
  }

  @Test
  void sendEvent_requestTimeout() {
    tesmonEventClient = new DefaultTesmonEventClient("1001", "api-token", "http://localhost:8080");
    // Start WireMock server
    WireMockServer wireMockServer = new WireMockServer(options().port(8080));
    wireMockServer.start();

    // Configure WireMock to simulate a request timeout
    configureFor("localhost", 8080);
    stubFor(post(urlEqualTo("/v1/environments/1001/events"))
        .willReturn(aResponse().withFixedDelay(60000)));

    Future<String> future = tesmonEventClient.sendEvent(EVENT_KEY, EVENT_BODY);
    ExecutionException exception = assertThrows(ExecutionException.class, future::get);
    assertTrue(exception.getCause() instanceof java.net.SocketTimeoutException);

    // Stop WireMock server
    wireMockServer.stop();
  }

  @Test
  void sendEvent_invalidEnvironmentId() throws ExecutionException, InterruptedException, TimeoutException {
    tesmonEventClient = new DefaultTesmonEventClient("1001", "api-token", "http://localhost:8080");
    String expectedEventId = UUID.randomUUID().toString();
    JSONObject expected = new JSONObject();
    expected.put("error", "Environment with ID: 1001 not found");

    // Start WireMock server
    WireMockServer wireMockServer = new WireMockServer(options().port(8080));
    wireMockServer.start();

    // Configure WireMock to return a mock response from localhost
    configureFor("localhost", 8080);
    stubFor(post(urlEqualTo("/v1/environments/1001/events"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_BAD_REQUEST)
            .withHeader("Content-Type", "application/json")
            .withBody("{ \"error\": \"Environment with ID: 1001 not found\" }")));

    Future<String> future = tesmonEventClient.sendEvent(EVENT_KEY, EVENT_BODY);
    String actual = future.get(5, TimeUnit.SECONDS);
    JSONObject actualJson = new JSONObject(actual);
    assertEquals(expected.toString(), actualJson.toString());

    // Stop WireMock server
    wireMockServer.stop();
  }

  @Test
  void testDefaultConstructorWithNullEnvironmentId() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new DefaultTesmonEventClient(null, "api-token", "http://localhost:8080").sendEvent("abc", null));
    assertEquals("environmentId is null or empty", exception.getMessage());
  }

  @Test
  void testDefaultConstructorWithNullToken() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new DefaultTesmonEventClient("1001", null, "http://localhost:8080").sendEvent("abc", null));
    assertEquals("apiToken is null or empty", exception.getMessage());
  }
}
