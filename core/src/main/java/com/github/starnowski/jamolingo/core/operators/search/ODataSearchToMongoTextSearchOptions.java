package com.github.starnowski.jamolingo.core.operators.search;

public interface ODataSearchToMongoTextSearchOptions {

  /**
   * Sets the minimum text score required for a document to be returned.
   *
   * @param defaultTextScore the minimum text score
   */
  void setDefaultTextScore(Double defaultTextScore);

  /**
   * Returns the minimum text score required for a document to be returned.
   *
   * @return the minimum text score, or null if not set
   */
  Double getDefaultTextScore();
}
