package com.github.starnowski.jamolingo.context;

import java.util.Objects;

public class DefaultEdmPathContextSearch implements EdmPathContextSearch {
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

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Integer mongoPathMaxDepth;
    private Integer maxCircularLimitPerEdmPath;
    private Integer maxCircularLimitForAllEdmPaths;

    public Builder withMaxCircularLimitPerEdmPath(Integer maxCircularLimitPerEdmPath) {
      this.maxCircularLimitPerEdmPath = maxCircularLimitPerEdmPath;
      return this;
    }

    public Builder withMongoPathMaxDepth(Integer mongoPathMaxDepth) {
      this.mongoPathMaxDepth = mongoPathMaxDepth;
      return this;
    }

    public Builder withMaxCircularLimitForAllEdmPaths(Integer maxCircularLimitForAllEdmPaths) {
      this.maxCircularLimitForAllEdmPaths = maxCircularLimitForAllEdmPaths;
      return this;
    }

    public DefaultEdmPathContextSearch build() {
      return new DefaultEdmPathContextSearch(
          mongoPathMaxDepth, maxCircularLimitPerEdmPath, maxCircularLimitForAllEdmPaths);
    }
  }
}
