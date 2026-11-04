package com.github.starnowski.jamolingo.junit5;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Configuration holding wait duration, polling interval, and custom properties for Atlas Search
 * Index synchronization validation.
 */
public class AtlasSearchIndexValidationConfig {

  /** Default maximum wait duration (30). */
  public static final long DEFAULT_MAX_WAIT_DURATION = 30L;

  /** Default maximum wait time unit (SECONDS). */
  public static final TimeUnit DEFAULT_MAX_WAIT_UNIT = TimeUnit.SECONDS;

  /** Default polling interval (1). */
  public static final long DEFAULT_POLL_INTERVAL = 1L;

  /** Default polling interval time unit (SECONDS). */
  public static final TimeUnit DEFAULT_POLL_INTERVAL_UNIT = TimeUnit.SECONDS;

  private final long maxWaitDuration;
  private final TimeUnit maxWaitUnit;
  private final long pollInterval;
  private final TimeUnit pollIntervalUnit;
  private final Map<String, String> properties;

  /**
   * Constructs a new AtlasSearchIndexValidationConfig with an empty properties map.
   *
   * @param maxWaitDuration the maximum duration to wait
   * @param maxWaitUnit the time unit for the maximum wait duration
   * @param pollInterval the interval between polls
   * @param pollIntervalUnit the time unit for the polling interval
   */
  public AtlasSearchIndexValidationConfig(
      long maxWaitDuration, TimeUnit maxWaitUnit, long pollInterval, TimeUnit pollIntervalUnit) {
    this(maxWaitDuration, maxWaitUnit, pollInterval, pollIntervalUnit, Collections.emptyMap());
  }

  /**
   * Constructs a new AtlasSearchIndexValidationConfig.
   *
   * @param maxWaitDuration the maximum duration to wait
   * @param maxWaitUnit the time unit for the maximum wait duration
   * @param pollInterval the interval between polls
   * @param pollIntervalUnit the time unit for the polling interval
   * @param properties custom configuration properties
   */
  public AtlasSearchIndexValidationConfig(
      long maxWaitDuration,
      TimeUnit maxWaitUnit,
      long pollInterval,
      TimeUnit pollIntervalUnit,
      Map<String, String> properties) {
    this.maxWaitDuration = maxWaitDuration;
    this.maxWaitUnit = maxWaitUnit;
    this.pollInterval = pollInterval;
    this.pollIntervalUnit = pollIntervalUnit;
    this.properties =
        properties == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(new LinkedHashMap<>(properties));
  }

  /**
   * Returns the maximum duration to wait.
   *
   * @return the maximum wait duration
   */
  public long getMaxWaitDuration() {
    return maxWaitDuration;
  }

  /**
   * Returns the time unit for the maximum wait duration.
   *
   * @return the maximum wait time unit
   */
  public TimeUnit getMaxWaitUnit() {
    return maxWaitUnit;
  }

  /**
   * Returns the interval between polls.
   *
   * @return the polling interval
   */
  public long getPollInterval() {
    return pollInterval;
  }

  /**
   * Returns the time unit for the polling interval.
   *
   * @return the polling interval time unit
   */
  public TimeUnit getPollIntervalUnit() {
    return pollIntervalUnit;
  }

  /**
   * Returns the unmodifiable map of custom configuration properties.
   *
   * @return the custom properties map
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AtlasSearchIndexValidationConfig that = (AtlasSearchIndexValidationConfig) o;
    return maxWaitDuration == that.maxWaitDuration
        && pollInterval == that.pollInterval
        && maxWaitUnit == that.maxWaitUnit
        && pollIntervalUnit == that.pollIntervalUnit
        && Objects.equals(properties, that.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(maxWaitDuration, maxWaitUnit, pollInterval, pollIntervalUnit, properties);
  }

  @Override
  public String toString() {
    return "AtlasSearchIndexValidationConfig{"
        + "maxWaitDuration="
        + maxWaitDuration
        + ", maxWaitUnit="
        + maxWaitUnit
        + ", pollInterval="
        + pollInterval
        + ", pollIntervalUnit="
        + pollIntervalUnit
        + ", properties="
        + properties
        + '}';
  }

  /**
   * Creates a new builder instance for AtlasSearchIndexValidationConfig.
   *
   * @return a new Builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link AtlasSearchIndexValidationConfig}. */
  public static class Builder {
    private long maxWaitDuration = DEFAULT_MAX_WAIT_DURATION;
    private TimeUnit maxWaitUnit = DEFAULT_MAX_WAIT_UNIT;
    private long pollInterval = DEFAULT_POLL_INTERVAL;
    private TimeUnit pollIntervalUnit = DEFAULT_POLL_INTERVAL_UNIT;
    private Map<String, String> properties = Collections.emptyMap();

    /**
     * Copies properties from an existing {@link AtlasSearchIndexValidationConfig}.
     *
     * @param config the existing configuration to copy from
     * @return this builder instance
     */
    public Builder withAtlasSearchIndexValidationConfig(AtlasSearchIndexValidationConfig config) {
      this.maxWaitDuration = config.maxWaitDuration;
      this.maxWaitUnit = config.maxWaitUnit;
      this.pollInterval = config.pollInterval;
      this.pollIntervalUnit = config.pollIntervalUnit;
      this.properties = config.properties;
      return this;
    }

    /**
     * Sets the maximum duration to wait.
     *
     * @param maxWaitDuration the maximum wait duration
     * @return this builder instance
     */
    public Builder withMaxWaitDuration(long maxWaitDuration) {
      this.maxWaitDuration = maxWaitDuration;
      return this;
    }

    /**
     * Sets the time unit for the maximum wait duration.
     *
     * @param maxWaitUnit the maximum wait time unit
     * @return this builder instance
     */
    public Builder withMaxWaitUnit(TimeUnit maxWaitUnit) {
      this.maxWaitUnit = maxWaitUnit;
      return this;
    }

    /**
     * Sets the interval between polls.
     *
     * @param pollInterval the polling interval
     * @return this builder instance
     */
    public Builder withPollInterval(long pollInterval) {
      this.pollInterval = pollInterval;
      return this;
    }

    /**
     * Sets the time unit for the polling interval.
     *
     * @param pollIntervalUnit the polling interval time unit
     * @return this builder instance
     */
    public Builder withPollIntervalUnit(TimeUnit pollIntervalUnit) {
      this.pollIntervalUnit = pollIntervalUnit;
      return this;
    }

    /**
     * Sets the custom configuration properties map.
     *
     * @param properties the custom properties map
     * @return this builder instance
     */
    public Builder withProperties(Map<String, String> properties) {
      this.properties = properties;
      return this;
    }

    /**
     * Builds a new {@link AtlasSearchIndexValidationConfig} instance.
     *
     * @return a new AtlasSearchIndexValidationConfig
     */
    public AtlasSearchIndexValidationConfig build() {
      return new AtlasSearchIndexValidationConfig(
          maxWaitDuration, maxWaitUnit, pollInterval, pollIntervalUnit, properties);
    }
  }
}
