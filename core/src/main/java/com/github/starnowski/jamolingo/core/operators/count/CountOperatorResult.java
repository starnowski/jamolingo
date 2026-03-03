package com.github.starnowski.jamolingo.core.operators.count;

import com.github.starnowski.jamolingo.core.operators.OlingoOperatorResult;

/** Represents the result of processing an OData $count system query option. */
public interface CountOperatorResult extends OlingoOperatorResult {

  /**
   * Returns the name of the count field.
   *
   * @return the count field name
   */
  String getCountFieldName();

  /**
   * Returns whether the $count option is present and enabled.
   *
   * @return true if $count is present and true, false otherwise
   */
  boolean isCountOptionPresent();
}
