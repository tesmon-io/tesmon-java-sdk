package io.tesmon;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

public class DefaultTesmonEventClient implements TesmonEventClient {
  private String tesmonTreBaseUrl;

  public DefaultTesmonEventClient(String tesmonTreBaseUrl) {
    this.tesmonTreBaseUrl = tesmonTreBaseUrl;
  }

  public DefaultTesmonEventClient() {
    this.tesmonTreBaseUrl = System.getenv("TESMON_TRE_BASE_URL");
  }

  @Override
  public String sendEvent(String eventKey, JSONObject eventBody) {
    if (tesmonTreBaseUrl == null || tesmonTreBaseUrl.isEmpty()) {
      throw new IllegalArgumentException("Tesmon Test Run Engine base URL is null or empty");
    }
    String url = tesmonTreBaseUrl + "/v1/events";

    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost(url);

    JSONObject event = new JSONObject();
    event.append("eventKey", eventKey);
    event.append("eventBody", eventBody);
    event.append("createdAt", System.currentTimeMillis());

    StringEntity payloadEntity = new StringEntity(event.toString(), ContentType.APPLICATION_JSON);
    httpPost.setEntity(payloadEntity);

    try {
      HttpResponse httpResponse = httpClient.execute(httpPost);
      int statusCode = httpResponse.getStatusLine().getStatusCode();

      String responseBody = EntityUtils.toString(httpResponse.getEntity());
      if (statusCode != HttpStatus.SC_OK) {
        System.err.println("Failed to send event. HTTP status code: " + statusCode);
      }

      return responseBody;
    } catch (IOException e) {
      System.err.println("Failed to send event. Exception: " + e.getMessage());
      return null;
    }
  }
}
