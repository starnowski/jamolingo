package com.github.starnowski.jamolingo.core.operators.search;

import static com.github.starnowski.jamolingo.core.operators.search.ODataSearchToMongoAtlasSearchParser.SEARCH_SCORE_DEFAULT_VARIABLE;

public interface ODataSearchToMongoTextSearchOptions {

  /**
   * Sets the minimum text score required for a document to be returned.
   *
   * @param defaultTextScore the minimum text score
   */
  default void setDefaultTextScore(Double defaultTextScore) {
    setDefaultTextScore(defaultTextScore, SEARCH_SCORE_DEFAULT_VARIABLE);
  }

  /**
   * Sets the minimum text score and the field name for the score value.
   *
   * @param defaultTextScore the minimum text score
   * @param scoreFieldName the name of the field that will store the score value
   */
  void setDefaultTextScore(Double defaultTextScore, String scoreFieldName);

  /**
   * Returns the minimum text score required for a document to be returned.
   *
   * @return the minimum text score, or null if not set
   */
  Double getDefaultTextScore();

  /**
   * Returns the name of the field that will store the text search score value.
   *
   * @return the name of the field
   */
  String getScoreFieldName();
}
