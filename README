# Tesmon Java SDK

This is the official Tesmon Java SDK.

## Installation

To use the Tesmon Java SDK in your project, add the following dependency to your `pom.xml` file:

```xml
<dependency>
  <groupId>com.tesmon</groupId>
  <artifactId>tesmon-java-sdk</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Usage
To use the Tesmon Java SDK in your project, first create a TesmonEventClient object using one of the available constructors:
```java
TesmonEventClient tesmonEventClient = new DefaultTesmonEventClient("http://localhost:8080");
```

Then, use the ```sendEvent``` method to send an event to Tesmon:
```java
JSONObject eventBody = new JSONObject().put("key", "value");
String eventId = tesmonEventClient.sendEvent("eventKey", eventBody);
```

## License
[MIT License](https://opensource.org/licenses/MIT)


