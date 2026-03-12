package com.github.starnowski.jamolingo.core.operators.search;

import java.util.Objects;

/** Default implementation of ODataSearchToMongoAtlasSearchOptions. */
public class DefaultODataSearchToMongoAtlasSearchOptions
    implements ODataSearchToMongoAtlasSearchOptions {

  private Double defaultTextScore;

  @Override
  public void setDefaultTextScore(Double defaultTextScore) {
    this.defaultTextScore = defaultTextScore;
  }

  @Override
  public Double getDefaultTextScore() {
    return defaultTextScore;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DefaultODataSearchToMongoAtlasSearchOptions that =
        (DefaultODataSearchToMongoAtlasSearchOptions) o;
    return Objects.equals(defaultTextScore, that.defaultTextScore);
  }

  @Override
  public int hashCode() {
    return Objects.hash(defaultTextScore);
  }

  @Override
  public String toString() {
    return "DefaultODataSearchToMongoAtlasSearchOptions{"
        + "defaultTextScore="
        + defaultTextScore
        + '}';
  }

  /**
   * Returns a new builder for DefaultODataSearchToMongoAtlasSearchOptions.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for DefaultODataSearchToMongoAtlasSearchOptions. */
  public static class Builder {

    private Double defaultTextScore;

    public Builder withDefaultTextScore(Double defaultTextScore) {
      this.defaultTextScore = defaultTextScore;
      return this;
    }

    /**
     * Builds a new DefaultODataSearchToMongoAtlasSearchOptions instance.
     *
     * @return a new instance
     */
    public DefaultODataSearchToMongoAtlasSearchOptions build() {
      DefaultODataSearchToMongoAtlasSearchOptions options =
          new DefaultODataSearchToMongoAtlasSearchOptions();
      options.setDefaultTextScore(defaultTextScore);
      return options;
    }
  }
}
