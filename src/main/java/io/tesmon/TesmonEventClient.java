package io.tesmon;

import org.json.JSONObject;

/**
 * This interface provides methods to send events to Tesmon.
 */
public interface TesmonEventClient {
  /**
   * Sends an event to Tesmon.
   * @param eventKey Unique event key to use for the event.
   * @param eventBody A JSON object representing the body of the event.
   * @return A JSON string containing the "eventId" field with the ID of the created event, or a "message" field with the error message if the event could not be created or sent.
   */
  String sendEvent(String eventKey, JSONObject eventBody);
}
