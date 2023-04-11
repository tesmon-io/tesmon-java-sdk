package io.tesmon;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

public class DefaultTesmonEventClient implements TesmonEventClient {
  private final static int DEFAULT_TIMEOUT = 5000;
  private String tesmonTreBaseUrl;

  public DefaultTesmonEventClient(String tesmonTreBaseUrl) {
    this.tesmonTreBaseUrl = tesmonTreBaseUrl;
  }

  public DefaultTesmonEventClient() {
    this.tesmonTreBaseUrl = System.getenv("TESMON_TRE_BASE_URL");
  }

  @Override
  public CompletableFuture<String> sendEvent(String eventKey, JSONObject eventBody) {
    if (tesmonTreBaseUrl == null || tesmonTreBaseUrl.isEmpty()) {
      throw new IllegalArgumentException("Tesmon Test Run Engine base URL is null or empty");
    }
    String url = tesmonTreBaseUrl + "/v1/events";

    CloseableHttpAsyncClient httpClient = HttpAsyncClients.custom()
        .setDefaultRequestConfig(RequestConfig.custom()
            .setConnectionRequestTimeout(DEFAULT_TIMEOUT)
            .setConnectTimeout(DEFAULT_TIMEOUT)
            .setSocketTimeout(DEFAULT_TIMEOUT)
            .build())
        .build();

    CompletableFuture<String> future = new CompletableFuture<>();

    try {
      httpClient.start();

      HttpPost httpPost = new HttpPost(url);

      JSONObject event = new JSONObject();
      event.append("eventKey", eventKey);
      event.append("eventBody", eventBody);
      event.append("createdAt", System.currentTimeMillis());

      StringEntity payloadEntity = new StringEntity(event.toString(), ContentType.APPLICATION_JSON.withCharset(Charset.forName("UTF-8")));
      httpPost.setEntity(payloadEntity);

      httpClient.execute(httpPost, new FutureCallback<>() {
        @Override
        public void completed(HttpResponse result) {
          try {
            int statusCode = result.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(result.getEntity());

            if (statusCode != HttpStatus.SC_OK) {
              System.err.println("Failed to send event. HTTP status code: " + statusCode + ", body: " + responseBody);
            }

            future.complete(responseBody);
          } catch (IOException e) {
            future.completeExceptionally(e);
          }
        }

        @Override
        public void failed(Exception ex) {
          future.completeExceptionally(ex);
        }

        @Override
        public void cancelled() {
          future.cancel(true);
        }
      });
    } catch (Exception e) {
      System.err.println("Failed to send event. Exception: " + e.getMessage());
      future.completeExceptionally(e);
    }

    return future;
  }
}

