package com.github.starnowski.jamolingo.context;

import java.util.Objects;

public class DefaultEdmPathContextSearch implements EdmPathContextSearch {
  public DefaultEdmPathContextSearch(
      Integer mongoPathMaxDepth, Integer maxCircularLimitPerEdmPath) {
    this.mongoPathMaxDepth = mongoPathMaxDepth;
    this.maxCircularLimitPerEdmPath = maxCircularLimitPerEdmPath;
  }

  private final Integer mongoPathMaxDepth;
  private final Integer maxCircularLimitPerEdmPath;

  @Override
  public Integer getMongoPathMaxDepth() {
    return mongoPathMaxDepth;
  }

  @Override
  public Integer getMaxCircularLimitPerEdmPath() {
    return maxCircularLimitPerEdmPath;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    DefaultEdmPathContextSearch that = (DefaultEdmPathContextSearch) o;
    return Objects.equals(mongoPathMaxDepth, that.mongoPathMaxDepth);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mongoPathMaxDepth);
  }

  @Override
  public String toString() {
    return "DefaultEdmPathContextSearch{" + "mongoPathMaxDepth=" + mongoPathMaxDepth + '}';
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Integer mongoPathMaxDepth;
    private Integer maxCircularLimitPerEdmPath;

    public Builder withMaxCircularLimitPerEdmPath(Integer maxCircularLimitPerEdmPath) {
      this.maxCircularLimitPerEdmPath = maxCircularLimitPerEdmPath;
      return this;
    }

    public Builder withMongoPathMaxDepth(Integer mongoPathMaxDepth) {
      this.mongoPathMaxDepth = mongoPathMaxDepth;
      return this;
    }

    public DefaultEdmPathContextSearch build() {
      return new DefaultEdmPathContextSearch(mongoPathMaxDepth, maxCircularLimitPerEdmPath);
    }
  }
}
