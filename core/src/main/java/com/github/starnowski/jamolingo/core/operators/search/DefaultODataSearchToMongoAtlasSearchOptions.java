package com.github.starnowski.jamolingo.core.operators.search;

import java.util.Objects;

/** Default implementation of ODataSearchToMongoAtlasSearchOptions. */
public class DefaultODataSearchToMongoAtlasSearchOptions
    implements ODataSearchToMongoAtlasSearchOptions {

  private Double defaultTextScore;
  private String scoreFieldName;

  @Override
  public void setDefaultTextScore(Double defaultTextScore, String scoreFieldName) {
    this.defaultTextScore = defaultTextScore;
    this.scoreFieldName = scoreFieldName;
  }

  @Override
  public Double getDefaultTextScore() {
    return defaultTextScore;
  }

  @Override
  public String getScoreFieldName() {
    return scoreFieldName;
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
    return Objects.equals(defaultTextScore, that.defaultTextScore)
        && Objects.equals(scoreFieldName, that.scoreFieldName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(defaultTextScore, scoreFieldName);
  }

  @Override
  public String toString() {
    return "DefaultODataSearchToMongoAtlasSearchOptions{"
        + "defaultTextScore="
        + defaultTextScore
        + ", scoreFieldName='"
        + scoreFieldName
        + '\''
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
    private String scoreFieldName =
        ODataSearchToMongoAtlasSearchParser.SEARCH_SCORE_DEFAULT_VARIABLE;

    /**
     * Sets the minimum text score.
     *
     * @param defaultTextScore the default text score
     * @return the builder instance
     */
    public Builder withDefaultTextScore(Double defaultTextScore) {
      this.defaultTextScore = defaultTextScore;
      return this;
    }

    /**
     * Sets the score field name.
     *
     * @param scoreFieldName the name of the field to store the score
     * @return the builder instance
     */
    public Builder withScoreFieldName(String scoreFieldName) {
      this.scoreFieldName = scoreFieldName;
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
      options.setDefaultTextScore(defaultTextScore, scoreFieldName);
      return options;
    }
  }
}
