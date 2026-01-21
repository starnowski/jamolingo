package com.github.starnowski.jamolingo.context;

import static com.github.starnowski.jamolingo.core.utils.Constants.MONGO_HARDCODED_BSON_DOCUMENT_NESTING_LIMIT;

import java.util.Objects;

/** Default implementation of {@link EdmPathContextSearch}. */
public class DefaultEdmPathContextSearch implements EdmPathContextSearch {
  /**
   * Constructs a new DefaultEdmPathContextSearch.
   *
   * @param mongoPathMaxDepth the maximum depth for Mongo paths
   * @param maxCircularLimitPerEdmPath the maximum circular limit per EDM path
   * @param maxCircularLimitForAllEdmPaths the maximum circular limit for all EDM paths
   */
  public DefaultEdmPathContextSearch(
      Integer mongoPathMaxDepth,
      Integer maxCircularLimitPerEdmPath,
      Integer maxCircularLimitForAllEdmPaths) {
    this.mongoPathMaxDepth = mongoPathMaxDepth;
    this.maxCircularLimitPerEdmPath = maxCircularLimitPerEdmPath;
    this.maxCircularLimitForAllEdmPaths = maxCircularLimitForAllEdmPaths;
  }

  private final Integer mongoPathMaxDepth;
  private final Integer maxCircularLimitPerEdmPath;
  private final Integer maxCircularLimitForAllEdmPaths;

  @Override
  public Integer getMongoPathMaxDepth() {
    return mongoPathMaxDepth;
  }

  @Override
  public Integer getMaxCircularLimitPerEdmPath() {
    return maxCircularLimitPerEdmPath;
  }

  @Override
  public Integer getMaxCircularLimitForAllEdmPaths() {
    return maxCircularLimitForAllEdmPaths;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DefaultEdmPathContextSearch that = (DefaultEdmPathContextSearch) o;
    return Objects.equals(mongoPathMaxDepth, that.mongoPathMaxDepth)
        && Objects.equals(maxCircularLimitPerEdmPath, that.maxCircularLimitPerEdmPath)
        && Objects.equals(maxCircularLimitForAllEdmPaths, that.maxCircularLimitForAllEdmPaths);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        mongoPathMaxDepth, maxCircularLimitPerEdmPath, maxCircularLimitForAllEdmPaths);
  }

  @Override
  public String toString() {
    return "DefaultEdmPathContextSearch{"
        + "mongoPathMaxDepth="
        + mongoPathMaxDepth
        + ", maxCircularLimitPerEdmPath="
        + maxCircularLimitPerEdmPath
        + ", maxCircularLimitForAllEdmPaths="
        + maxCircularLimitForAllEdmPaths
        + '}';
  }

  /**
   * Creates a new builder for DefaultEdmPathContextSearch.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for DefaultEdmPathContextSearch. */
  public static class Builder {
    private Integer mongoPathMaxDepth = MONGO_HARDCODED_BSON_DOCUMENT_NESTING_LIMIT;
    private Integer maxCircularLimitPerEdmPath;
    private Integer maxCircularLimitForAllEdmPaths;

    /**
     * Initializes the builder with values from an existing DefaultEdmPathContextSearch.
     *
     * @param defaultEdmPathContextSearch the existing instance
     * @return the builder instance
     */
    public Builder withDefaultEdmPathContextSearch(
        DefaultEdmPathContextSearch defaultEdmPathContextSearch) {
      this.maxCircularLimitPerEdmPath = defaultEdmPathContextSearch.maxCircularLimitPerEdmPath;
      this.mongoPathMaxDepth = defaultEdmPathContextSearch.mongoPathMaxDepth;
      this.maxCircularLimitForAllEdmPaths =
          defaultEdmPathContextSearch.maxCircularLimitForAllEdmPaths;
      return this;
    }

    /**
     * Sets the maximum circular limit per EDM path.
     *
     * @param maxCircularLimitPerEdmPath the limit
     * @return the builder instance
     */
    public Builder withMaxCircularLimitPerEdmPath(Integer maxCircularLimitPerEdmPath) {
      this.maxCircularLimitPerEdmPath = maxCircularLimitPerEdmPath;
      return this;
    }

    /**
     * Sets the maximum depth for Mongo paths.
     *
     * @param mongoPathMaxDepth the maximum depth
     * @return the builder instance
     */
    public Builder withMongoPathMaxDepth(Integer mongoPathMaxDepth) {
      this.mongoPathMaxDepth = mongoPathMaxDepth;
      return this;
    }

    /**
     * Sets the maximum circular limit for all EDM paths.
     *
     * @param maxCircularLimitForAllEdmPaths the limit
     * @return the builder instance
     */
    public Builder withMaxCircularLimitForAllEdmPaths(Integer maxCircularLimitForAllEdmPaths) {
      this.maxCircularLimitForAllEdmPaths = maxCircularLimitForAllEdmPaths;
      return this;
    }

    /**
     * Builds the DefaultEdmPathContextSearch.
     *
     * @return the new instance
     */
    public DefaultEdmPathContextSearch build() {
      return new DefaultEdmPathContextSearch(
          mongoPathMaxDepth, maxCircularLimitPerEdmPath, maxCircularLimitForAllEdmPaths);
    }
  }
}
