package com.github.starnowski.jamolingo.junit5;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AtlasSearchIndexValidationConfigTest {

  @Test
  void shouldBuildWithDefaultValues() {
    // WHEN
    AtlasSearchIndexValidationConfig config = AtlasSearchIndexValidationConfig.builder().build();

    // THEN
    assertEquals(30L, config.getMaxWaitDuration());
    assertEquals(SECONDS, config.getMaxWaitUnit());
    assertEquals(1L, config.getPollInterval());
    assertEquals(SECONDS, config.getPollIntervalUnit());
    assertTrue(config.getProperties().isEmpty());
  }

  @Test
  void shouldBuildWithCustomValuesAndCopyFromExisting() {
    // GIVEN
    AtlasSearchIndexValidationConfig original =
        AtlasSearchIndexValidationConfig.builder()
            .withMaxWaitDuration(15L)
            .withMaxWaitUnit(SECONDS)
            .withPollInterval(250L)
            .withPollIntervalUnit(MILLISECONDS)
            .withProperties(Map.of("key1", "val1"))
            .build();

    // WHEN
    AtlasSearchIndexValidationConfig copy =
        AtlasSearchIndexValidationConfig.builder()
            .withAtlasSearchIndexValidationConfig(original)
            .build();
    AtlasSearchIndexValidationConfig modified =
        AtlasSearchIndexValidationConfig.builder()
            .withAtlasSearchIndexValidationConfig(original)
            .withMaxWaitDuration(45L)
            .build();

    // THEN
    assertEquals(original, copy);
    assertEquals(original.hashCode(), copy.hashCode());
    assertEquals(original.toString(), copy.toString());
    assertNotEquals(original, modified);
    assertEquals(15L, original.getMaxWaitDuration());
    assertEquals(SECONDS, original.getMaxWaitUnit());
    assertEquals(250L, original.getPollInterval());
    assertEquals(MILLISECONDS, original.getPollIntervalUnit());
    assertEquals(Map.of("key1", "val1"), original.getProperties());
  }
}
