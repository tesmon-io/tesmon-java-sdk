package io.tesmon;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TesmonEventClientTests {

  private static final String BASE_URL = "http://localhost:8080";
  private static final String EVENT_KEY = "testEvent";
  private static final JSONObject EVENT_BODY = new JSONObject().put("key", "value");

  private TesmonEventClient tesmonEventClient;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void sendEvent_successful() {
    tesmonEventClient = new DefaultTesmonEventClient(BASE_URL);
    String expectedEventId = UUID.randomUUID().toString();
    JSONObject expected = new JSONObject();
    expected.put("eventId", expectedEventId);

    // Start WireMock server
    WireMockServer wireMockServer = new WireMockServer(options().port(8080));
    wireMockServer.start();

    // Configure WireMock to return a mock response from localhost
    configureFor("localhost", 8080);
    stubFor(post(urlEqualTo("/v1/events"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", "application/json")
            .withBody("{ \"eventId\": \"" + expectedEventId + "\" }")));

    String actual = tesmonEventClient.sendEvent(EVENT_KEY, EVENT_BODY);
    JSONObject actualJson = new JSONObject(actual);
    assertEquals(expected.toString(), actualJson.toString());

    // Stop WireMock server
    wireMockServer.stop();
  }

  @Test
  void sendEvent_badRequest() throws IOException {
    tesmonEventClient = new DefaultTesmonEventClient(BASE_URL);
    // Start WireMock server
    WireMockServer wireMockServer = new WireMockServer(options().port(8080));
    wireMockServer.start();

    // Configure WireMock to return a bad request response from localhost
    configureFor("localhost", 8080);
    stubFor(post(urlEqualTo("/v1/events"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_BAD_REQUEST)
            .withHeader("Content-Type", "application/json")
            .withBody("{ \"message\": \"Invalid request body\" }")));

    String actual = tesmonEventClient.sendEvent(EVENT_KEY, EVENT_BODY);
    JSONObject actualJson = new JSONObject(actual);
    assertEquals(new JSONObject("{\"message\": \"Invalid request body\"}").toString(), actualJson.toString());

    // Stop WireMock server
    wireMockServer.stop();
  }

  @Test
  void sendEvent_internalServerError() throws IOException {
    tesmonEventClient = new DefaultTesmonEventClient(BASE_URL);
    // Start WireMock server
    WireMockServer wireMockServer = new WireMockServer(options().port(8080));
    wireMockServer.start();

    // Configure WireMock to return an internal server error response from localhost
    configureFor("localhost", 8080);
    stubFor(post(urlEqualTo("/v1/events"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)
            .withHeader("Content-Type", "application/json")
            .withBody("{ \"message\": \"Internal server error\" }")));

    String actual = tesmonEventClient.sendEvent(EVENT_KEY, EVENT_BODY);
    JSONObject actualJson = new JSONObject(actual);
    assertEquals(new JSONObject("{\"message\": \"Internal server error\"}").toString(), actualJson.toString());
    // Stop WireMock server
    wireMockServer.stop();
  }

  @Test
  void sendEvent_unexpectedResponse() throws IOException {
    tesmonEventClient = new DefaultTesmonEventClient(BASE_URL);
    // Start WireMock server
    WireMockServer wireMockServer = new WireMockServer(options().port(8080));
    wireMockServer.start();

    // Configure WireMock to return an unexpected response from localhost
    configureFor("localhost", 8080);
    stubFor(post(urlEqualTo("/v1/events"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_BAD_REQUEST)
            .withHeader("Content-Type", "application/xml")
            .withBody("{ \"message\": \"Unexpected response\" }")));

    String actual = tesmonEventClient.sendEvent(EVENT_KEY, EVENT_BODY);
    JSONObject actualJson = new JSONObject(actual);
    assertEquals(new JSONObject("{\"message\": \"Unexpected response\"}").toString(), actualJson.toString());

    // Stop WireMock server
    wireMockServer.stop();
  }

  @Test
  void testDefaultConstructorWithNullBaseUrlVariable() {
    tesmonEventClient = new DefaultTesmonEventClient(null);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new DefaultTesmonEventClient().sendEvent("abc", null));
    assertEquals("Tesmon Test Run Engine base URL is null or empty", exception.getMessage());
  }

  @Test
  void testDefaultConstructorWithEmptyBaseUrlVariable() {
    tesmonEventClient = new DefaultTesmonEventClient(new String(""));
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new DefaultTesmonEventClient().sendEvent("abc", null));
    assertEquals("Tesmon Test Run Engine base URL is null or empty", exception.getMessage());
  }
}
