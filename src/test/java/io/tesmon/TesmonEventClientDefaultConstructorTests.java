package io.tesmon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TesmonEventClientDefaultConstructorTests {
  @Test
  void testDefaultConstructorWithNullEnvVariable() {
    assertThrows(IllegalArgumentException.class, () -> new DefaultTesmonEventClient().sendEvent("abc", null));
  }
}
